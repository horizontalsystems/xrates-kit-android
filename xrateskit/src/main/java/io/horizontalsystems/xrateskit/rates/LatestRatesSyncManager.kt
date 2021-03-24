package io.horizontalsystems.xrateskit.rates

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.entities.LatestRate
import io.horizontalsystems.xrateskit.entities.LatestRateKey
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.ConcurrentHashMap

class LatestRatesSyncManager(
        private var currency: String,
        private val schedulerFactory: LatestRatesSchedulerFactory)
    : LatestRatesManager.Listener {

    private var coinTypes: List<CoinType> = listOf()
    private val subjects = ConcurrentHashMap<LatestRateKey, PublishSubject<LatestRate>>()
    private val currencySubjects = ConcurrentHashMap<String, PublishSubject<Map<CoinType, LatestRate>>>()

    private var scheduler: LatestRatesScheduler? = null

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

    fun getLatestRateAsync(key: LatestRateKey): Observable<LatestRate>? {
        return subjects[key]
    }

    fun getLatestRateMapObservable(currency: String): Observable<Map<CoinType, LatestRate>> {
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

        coinTypes.forEach {
            subjects[LatestRateKey(it, currency)] = PublishSubject.create()
        }

        scheduler = schedulerFactory.getScheduler(coinTypes, currency)
        scheduler?.start()
    }

    //  LatestRateManager.Listener

    override fun onUpdate(latestRate: LatestRate, key: LatestRateKey) {
        subjects[key]?.onNext(latestRate)
    }

    override fun onUpdate(latestRateMap: Map<CoinType, LatestRate>, currency: String) {
        currencySubjects[currency]?.onNext(latestRateMap)
    }
}
