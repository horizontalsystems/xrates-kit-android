package io.horizontalsystems.xrateskit.marketinfo

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

class MarketInfoScheduler(private val provider: MarketInfoSchedulerProvider) {

    private var timeDisposable: Disposable? = null
    private var syncDisposable: Disposable? = null

    private var isExpiredRatesNotified = false
    private val bufferInterval = 5

    @Volatile
    private var stopped = false

    fun start(force: Boolean = false) {
        //  Force sync
        if (force) {
            onFire()
        } else {
            autoSchedule()
        }
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
            newDelay = max(0, provider.expirationInterval - bufferInterval - diff)
        }

        val delay = max(newDelay, minDelay)
        schedule(delay)
    }

    @Synchronized
    private fun schedule(delay: Long) {
        if (stopped) return

        notifyRatesIfExpired()

        timeDisposable?.dispose()
        timeDisposable = Observable
                .timer(delay, TimeUnit.SECONDS)
                .subscribe({
                    onFire()
                }, {
                    it.printStackTrace()
                })
    }

    private fun onFire() {
        if (stopped) return

        syncDisposable?.dispose()
        syncDisposable = provider.syncSingle
                .subscribe({
                    autoSchedule(provider.retryInterval)
                    isExpiredRatesNotified = false
                }, {
                    schedule(provider.retryInterval)
                })
    }

    private fun notifyRatesIfExpired() {
        if (isExpiredRatesNotified) return

        val timestamp = provider.lastSyncTimestamp
        if (timestamp == null || Date().time / 1000 - timestamp > provider.expirationInterval) {
            provider.notifyExpiredRates()
            isExpiredRatesNotified = true
        }
    }
}
