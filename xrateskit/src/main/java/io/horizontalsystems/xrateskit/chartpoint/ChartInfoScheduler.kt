package io.horizontalsystems.xrateskit.chartpoint

import io.horizontalsystems.xrateskit.entities.CryptoCompareError
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

class ChartInfoScheduler(private val provider: ChartInfoSchedulerProvider) {

    private var timeDisposable: Disposable? = null
    private var syncDisposable: Disposable? = null

    fun start() {
        GlobalScope.launch {
            autoSchedule()
        }
    }

    fun stop() {
        timeDisposable?.dispose()
        syncDisposable?.dispose()
    }

    private fun autoSchedule(minDelay: Long = 0) {
        var newDelay = 0L

        provider.lastSyncTimestamp?.let { lastSync ->
            val diff = Date().time / 1000 - lastSync
            newDelay = max(0L, provider.expirationInterval - diff)
        }

        schedule(max(minDelay, newDelay))
    }

    private fun schedule(delay: Long) {
        timeDisposable?.dispose()
        timeDisposable = Observable
                .timer(delay, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe {
                    onFire()
                }
    }

    private fun onFire() {
        syncDisposable?.dispose()
        syncDisposable = provider.syncSingle
                .subscribe({
                    autoSchedule(provider.retryInterval)
                }, {
                    if (it !is CryptoCompareError.NoDataForCoin) {
                        schedule(provider.retryInterval)
                    }
                })
    }

}
