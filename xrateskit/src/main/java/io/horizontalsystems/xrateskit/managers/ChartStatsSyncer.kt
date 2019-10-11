package io.horizontalsystems.xrateskit.managers

import io.horizontalsystems.xrateskit.core.*
import io.horizontalsystems.xrateskit.entities.ChartStats
import io.horizontalsystems.xrateskit.entities.ChartType
import io.reactivex.BackpressureStrategy
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class ChartStatsSyncer(
        private val storage: IStorage,
        private val subjectHolder: SubjectHolder,
        private val statsProvider: IChartStatsProvider) {

    private val listener: Listener? = null
    private val syncListener: ISyncCompletionListener? = null

    private val disposables = CompositeDisposable()

    interface Listener {
        fun onUpdate(stats: List<ChartStats>, coin: String, currency: String, type: ChartType)
    }

    fun subscribe(scheduler: SyncScheduler) {
        scheduler.eventSubject
                .toFlowable(BackpressureStrategy.BUFFER)
                .subscribe { state: SyncSchedulerEvent ->
                    when (state) {
                        SyncSchedulerEvent.FIRE -> onFire()
                        SyncSchedulerEvent.STOP -> onStop()
                    }
                }
                .let {
                    disposables.add(it)
                }
    }

    fun syncChartStats(coin: String, currency: String, type: ChartType) {
        statsProvider.getChartStats(coin, currency, type)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({
                    storage.saveChartStats(it)
                    listener?.onUpdate(it, coin, currency, type)
                }, {
                    syncListener?.onFail()
                })
                .let {
                    disposables.add(it)
                }
    }

    private fun onFire() {
        for (subjectKey in subjectHolder.activeChartStatsKeys) {
            val latestStats = storage.getLatestChartStats(subjectKey.coin, subjectKey.currency, subjectKey.type)
            if (latestStats == null || latestStats.timestamp < 0L) {
                syncChartStats(subjectKey.coin, subjectKey.currency, subjectKey.type)
            }
        }
    }

    private fun onStop() {
        disposables.clear()
    }
}
