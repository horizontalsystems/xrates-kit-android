package io.horizontalsystems.xrateskit.managers

import io.horizontalsystems.xrateskit.XRatesDataSource
import io.horizontalsystems.xrateskit.core.*
import io.horizontalsystems.xrateskit.entities.LatestRate
import io.reactivex.BackpressureStrategy
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

class LatestRateSyncer(
        private val storage: IStorage,
        private val dataSource: XRatesDataSource,
        private val rateProvider: ILatestRateProvider) {

    var listener: Listener? = null
    var syncListener: ISyncCompletionListener? = null

    private val disposables = CompositeDisposable()

    interface Listener {
        fun onUpdate(rate: LatestRate)
    }

    private var disposable: Disposable? = null

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


    fun sync() {
        if (dataSource.coins.isEmpty()) {
            return
        }

        disposable?.dispose()
        disposable = rateProvider.getLatestRate(dataSource.coins, dataSource.currency)
                .subscribe({
                    update(it)
                }, {
                    syncListener?.onFail()
                }, {
                    syncListener?.onSuccess()
                })
    }

    private fun update(rate: LatestRate) {
        storage.saveLatestRate(rate)
        listener?.onUpdate(rate)
    }

    private fun onFire() {
        sync()
    }

    private fun onStop() {
        disposable?.dispose()
        disposable = null
    }
}
