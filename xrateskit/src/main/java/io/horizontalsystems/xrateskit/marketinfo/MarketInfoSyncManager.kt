package io.horizontalsystems.xrateskit.marketinfo

import io.horizontalsystems.xrateskit.entities.MarketInfo
import io.horizontalsystems.xrateskit.entities.MarketInfoKey
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

class MarketInfoSyncManager(
        private var currency: String,
        private val schedulerFactory: MarketInfoSchedulerFactory)
    : MarketInfoManager.Listener {

    private var coins: List<String> = listOf()
    private val subjects = mutableMapOf<MarketInfoKey, PublishSubject<MarketInfo>>()
    private val currencySubjects = mutableMapOf<String, PublishSubject<Map<String, MarketInfo>>>()
    private var scheduler: MarketInfoScheduler? = null

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

    fun marketInfoObservable(key: MarketInfoKey): Observable<MarketInfo> {
        var subject = subjects[key]
        if (subject == null) {
            subject = PublishSubject.create<MarketInfo>()
            subjects[key] = subject
        }

        return subject
    }

    fun marketInfoMapObservable(currency: String): Observable<Map<String, MarketInfo>> {
        var subject = currencySubjects[currency]
        if (subject == null) {
            subject = PublishSubject.create<Map<String, MarketInfo>>()
            currencySubjects[currency] = subject
        }

        return subject
    }

    private fun updateScheduler() {
        scheduler?.stop()

        subjects.forEach { (key, subject) ->
            subject.onComplete()
            subjects.remove(key)
        }

        currencySubjects.forEach { (key, subject) ->
            subject.onComplete()
            currencySubjects.remove(key)
        }

        if (coins.isEmpty()) {
            return
        }

        scheduler = schedulerFactory.getScheduler(coins, currency)
        scheduler?.start()
    }

    //  LatestRateManager.Listener

    override fun onUpdate(marketInfo: MarketInfo, key: MarketInfoKey) {
        subjects[key]?.onNext(marketInfo)
    }

    override fun onUpdate(marketInfoMap: Map<String, MarketInfo>, currency: String) {
        currencySubjects[currency]?.onNext(marketInfoMap)
    }
}
