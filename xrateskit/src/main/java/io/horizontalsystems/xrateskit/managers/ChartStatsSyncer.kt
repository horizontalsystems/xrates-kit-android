package io.horizontalsystems.xrateskit.managers

import io.horizontalsystems.xrateskit.core.*
import io.horizontalsystems.xrateskit.entities.ChartStats
import io.horizontalsystems.xrateskit.entities.ChartType
import io.reactivex.BackpressureStrategy
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class ChartStatsSyncer(
        private val storage: IStorage,
        private val subjectHolder: SubjectHolder,
        private val statsProvider: IChartStatsProvider) {

    var listener: Listener? = null
    var syncListener: ISyncCompletionListener? = null

    private var schedulerDisposable: Disposable? = null
    private var syncDisposable: Disposable? = null

    interface Listener {
        fun onUpdate(stats: List<ChartStats>, coin: String, currency: String, chartType: ChartType)
    }

    fun subscribe(scheduler: SyncScheduler) {
        schedulerDisposable = scheduler.eventSubject
                .toFlowable(BackpressureStrategy.DROP)
                .subscribe { state: SyncSchedulerEvent ->
                    when (state) {
                        SyncSchedulerEvent.FIRE -> onFire()
                        SyncSchedulerEvent.STOP -> onStop()
                    }
                }
    }

    fun sync(coin: String, currency: String, chartType: ChartType) {
        syncDisposable = statsProvider.getChartStats(coin, currency, chartType)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({
                    storage.saveChartStats(it)
                    listener?.onUpdate(it, coin, currency, chartType)
                    syncListener?.onSuccess()
                }, {
                    syncListener?.onFail()
                })
    }

    private fun onFire() {
        for (subjectKey in subjectHolder.activeChartStatsKeys) {
            val latestStats = storage.getLatestChartStats(subjectKey.coin, subjectKey.currency, subjectKey.chartType)
            if (latestStats == null || subjectKey.chartType.isExpired(latestStats.timestamp)) {
                sync(subjectKey.coin, subjectKey.currency, subjectKey.chartType)
            }
        }
    }

    private fun onStop() {
        syncDisposable?.dispose()
        syncDisposable = null
    }
}
