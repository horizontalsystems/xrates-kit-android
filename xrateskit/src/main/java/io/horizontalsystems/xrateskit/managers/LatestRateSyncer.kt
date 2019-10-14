package io.horizontalsystems.xrateskit.managers

import io.horizontalsystems.xrateskit.XRatesDataSource
import io.horizontalsystems.xrateskit.core.*
import io.horizontalsystems.xrateskit.entities.LatestRate
import io.reactivex.BackpressureStrategy
import io.reactivex.disposables.Disposable

class LatestRateSyncer(
        private val storage: IStorage,
        private val dataSource: XRatesDataSource,
        private val rateProvider: ILatestRateProvider) {

    var listener: Listener? = null
    var syncListener: ISyncCompletionListener? = null

    private var schedulerDisposable: Disposable? = null
    private var syncDisposable: Disposable? = null

    interface Listener {
        fun onUpdate(latestRate: LatestRate)
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

    fun sync() {
        if (dataSource.currency == "" || dataSource.coins.isEmpty()) {
            return
        }

        syncDisposable?.dispose()
        syncDisposable = rateProvider.getLatestRate(dataSource.coins, dataSource.currency)
                .subscribe({ rate ->
                    storage.saveLatestRate(rate)
                    listener?.onUpdate(rate)
                }, {
                    syncListener?.onFail()
                }, {
                    syncListener?.onSuccess()
                })
    }

    private fun onFire() {
        sync()
    }

    private fun onStop() {
        syncDisposable?.dispose()
        syncDisposable = null
    }
}
