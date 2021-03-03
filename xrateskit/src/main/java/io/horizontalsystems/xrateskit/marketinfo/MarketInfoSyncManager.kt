package io.horizontalsystems.xrateskit.marketinfo

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.entities.MarketInfo
import io.horizontalsystems.xrateskit.entities.MarketInfoKey
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.ConcurrentHashMap

class MarketInfoSyncManager(
        private var currency: String,
        private val schedulerFactory: MarketInfoSchedulerFactory)
    : MarketInfoManager.Listener {

    private var coinTypes: List<CoinType> = listOf()
    private val subjects = ConcurrentHashMap<MarketInfoKey, PublishSubject<MarketInfo>>()
    private val currencySubjects = ConcurrentHashMap<String, PublishSubject<Map<CoinType, MarketInfo>>>()

    private var scheduler: MarketInfoScheduler? = null

    fun set(coinTypes: List<CoinType>) {
        this.coinTypes = coinTypes
        updateScheduler()
    }

    fun set(currencyCode: String) {
        if (currencyCode == currency) return

        currencySubjects.values.forEach { it.onComplete() }
        currencySubjects.clear()

        currency = currencyCode
        updateScheduler()
    }

    fun refresh() {
        scheduler?.start(force = true)
    }

    fun marketInfoObservable(key: MarketInfoKey): Observable<MarketInfo> {
        var subject = subjects[key]
        if (subject == null) {
            subject = PublishSubject.create()
            subjects[key] = subject
        }

        return subject
    }

    fun marketInfoMapObservable(currency: String): Observable<Map<CoinType, MarketInfo>> {
        var subject = currencySubjects[currency]
        if (subject == null) {
            subject = PublishSubject.create()
            currencySubjects[currency] = subject
        }

        return subject
    }

    private fun updateScheduler() {
        scheduler?.stop()
        scheduler = null

        subjects.forEach { (key, subject) ->
            subject.onComplete()
            subjects.remove(key)
        }

        if (coinTypes.isEmpty()) {
            return
        }

        scheduler = schedulerFactory.getScheduler(coinTypes, currency)
        scheduler?.start()
    }

    //  LatestRateManager.Listener

    override fun onUpdate(marketInfo: MarketInfo, key: MarketInfoKey) {
        subjects[key]?.onNext(marketInfo)
    }

    override fun onUpdate(marketInfoMap: Map<CoinType, MarketInfo>, currency: String) {
        currencySubjects[currency]?.onNext(marketInfoMap)
    }
}
