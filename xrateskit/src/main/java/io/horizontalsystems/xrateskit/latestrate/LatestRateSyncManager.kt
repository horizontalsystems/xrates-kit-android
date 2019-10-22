package io.horizontalsystems.xrateskit.latestrate

import io.horizontalsystems.xrateskit.entities.LatestRateKey
import io.horizontalsystems.xrateskit.entities.RateInfo
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class LatestRateSyncManager(
        private var currency: String,
        private val schedulerFactory: LatestRateSchedulerFactory)
    : LatestRateManager.Listener {

    private var coins: List<String> = listOf()
    private val subjects = mutableMapOf<LatestRateKey, PublishSubject<RateInfo>>()
    private var scheduler: LatestRateScheduler? = null

    fun set(coinCodes: List<String>) {
        coins = coinCodes
        updateScheduler()
    }

    fun set(currencyCode: String) {
        currency = currencyCode
        updateScheduler()
    }

    fun refresh() {
        scheduler?.start(force = true)
    }

    fun latestRateObservable(key: LatestRateKey): Observable<RateInfo> {
        var subject = subjects[key]
        if (subject == null) {
            subject = PublishSubject.create<RateInfo>()
            subjects[key] = subject
        }

        return subject
    }

    private fun updateScheduler() {
        subjects.clear()
        scheduler?.stop()

        if (coins.isEmpty()) {
            return
        }

        scheduler = schedulerFactory.getScheduler(coins, currency)
        scheduler?.start()
    }

    //  LatestRateManager.Listener

    override fun onUpdate(rate: RateInfo, key: LatestRateKey) {
        subjects[key]?.onNext(rate)
    }
}
