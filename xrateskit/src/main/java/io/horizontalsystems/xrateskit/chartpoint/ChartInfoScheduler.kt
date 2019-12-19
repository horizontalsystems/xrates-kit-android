package io.horizontalsystems.xrateskit.chartpoint

import io.horizontalsystems.xrateskit.entities.CryptoCompareError
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

class ChartInfoScheduler(private val provider: ChartInfoSchedulerProvider) {

    private var timeDisposable: Disposable? = null
    private var syncDisposable: Disposable? = null

    @Volatile
    private var stopped = false

    fun start() {
        autoSchedule()
    }

    @Synchronized
    fun stop() {
        stopped = true
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

    @Synchronized
    private fun schedule(delay: Long) {
        if (stopped) return

        timeDisposable?.dispose()
        timeDisposable = Observable
                .timer(delay, TimeUnit.SECONDS)
                .subscribe {
                    onFire()
                }
    }

    private fun onFire() {
        if (stopped) return

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
