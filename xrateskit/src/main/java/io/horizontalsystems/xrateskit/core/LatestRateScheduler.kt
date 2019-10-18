package io.horizontalsystems.xrateskit.core

import io.horizontalsystems.xrateskit.managers.LatestRateSyncer
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.max

class LatestRateScheduler(
        private val interval: Long,
        private val retryInterval: Long,
        private val latestRateSyncer: LatestRateSyncer) {

    private var disposable: Disposable? = null
    private var isExpiredRatesNotified = false
    private val bufferForNotifyExpired = 5

    fun start(force: Boolean = false) {
        //  Force sync
        if (force) {
            return onFire()
        }

        GlobalScope.launch {
            start()
        }
    }

    fun stop() {
        disposable?.dispose()
    }

    private fun start() {
        val lastSync = latestRateSyncer.lastSyncTimestamp ?: return schedule(0)
        val nextSync = lastSync - (Date().time / 1000 - interval)

        schedule(max(0, nextSync))
    }

    private fun schedule(delay: Long) {
        notifyRatesIfExpired()

        disposable?.dispose()
        disposable = Observable
                .timer(delay, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .subscribe({
                    onFire()
                }, {
                    it.printStackTrace()
                })
    }

    private fun onFire() {
        disposable?.dispose()
        disposable = latestRateSyncer.syncSingle
                .subscribe({
                    start()
                    isExpiredRatesNotified = false
                }, {
                    schedule(retryInterval)
                })
    }

    private fun notifyRatesIfExpired() {
        if (isExpiredRatesNotified) return

        val timestamp = latestRateSyncer.lastSyncTimestamp
        if (timestamp == null || timestamp < Date().time / 1000 - interval - bufferForNotifyExpired) {
            latestRateSyncer.notifyExpiredRates()
            isExpiredRatesNotified = true
        }
    }
}
