package io.horizontalsystems.xrateskit.latestrate

import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

class LatestRateScheduler(private val provider: LatestRateSchedulerProvider) {

    private var timeDisposable: Disposable? = null
    private var syncDisposable: Disposable? = null
    private var isExpiredRatesNotified = false
    private val bufferInterval = 5

    fun start(force: Boolean = false) {
        GlobalScope.launch {
            //  Force sync
            if (force) {
                onFire()
            } else {
                autoSchedule()
            }
        }
    }

    fun stop() {
        timeDisposable?.dispose()
        syncDisposable?.dispose()
    }

    private fun autoSchedule() {
        var newDelay = 0L

        provider.lastSyncTimestamp?.let { lastSync ->
            val diff = Date().time / 1000 - lastSync
            newDelay = max(0, provider.expirationInterval - bufferInterval - diff)
        }

        schedule(newDelay)
    }

    private fun schedule(delay: Long) {
        notifyRatesIfExpired()

        timeDisposable?.dispose()
        timeDisposable = Observable
                .timer(delay, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    onFire()
                }, {
                    it.printStackTrace()
                })
    }

    private fun onFire() {
        syncDisposable?.dispose()
        syncDisposable = provider.syncSingle
                .subscribe({
                    autoSchedule()
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
