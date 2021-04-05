package io.horizontalsystems.xrateskit.rates

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.entities.LatestRate
import io.horizontalsystems.xrateskit.entities.LatestRateKey
import io.horizontalsystems.xrateskit.entities.PairKey
import io.horizontalsystems.xrateskit.scheduler.Scheduler
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.ConcurrentHashMap

class LatestRatesSyncManager(
    private val schedulerFactory: LatestRatesSchedulerFactory
) : LatestRatesManager.Listener, ILatestRatesCoinTypeDataSource {

    private val schedulers = mutableMapOf<String, Scheduler>()
    private val subjects = ConcurrentHashMap<LatestRateKey, PublishSubject<Map<CoinType, LatestRate>>>()

    private fun observingCoinTypes(currencyCode: String): Set<CoinType> {
        subjects.forEach { (existKey, _) ->
            if (existKey.currencyCode == currencyCode) {
                return existKey.coinTypes.toSet()
            }
        }

        return setOf()
    }

    private fun needForceUpdate(key: LatestRateKey): Boolean {
        //get set of all listening coins
        //found tokens which needed to update
        //make new key for force update

        val newCoinTypes = key.coinTypes.minus(observingCoinTypes(key.currencyCode))
        return newCoinTypes.isNotEmpty()
    }

    private fun cleanUp(key: LatestRateKey) {
        val subject = subjects[key] ?: return
        if (subject.hasObservers()) return

        subject.onComplete()
        subjects.remove(key)

        if (subjects.none { it.key.currencyCode == key.currencyCode }) {
            schedulers[key.currencyCode]?.stop()
            schedulers.remove(key.currencyCode)
        }
    }

    private fun onDisposed(key: LatestRateKey) {
        cleanUp(key)
    }

    private fun subject(key: LatestRateKey):  Observable<Map<CoinType, LatestRate>> {
        val subject: PublishSubject<Map<CoinType, LatestRate>>
        var forceUpdate = false

        val candidate = subjects[key]
        if (candidate != null) {
            subject = candidate
        } else {                                        // create new subject
            forceUpdate = needForceUpdate(key)     // if subject has non-subscribed tokens we need force schedule

            subject = PublishSubject.create()
            subjects[key] = subject
        }

        if (schedulers[key.currencyCode] == null) {        // create scheduler if not exist
            val scheduler = schedulerFactory.getScheduler(key.currencyCode, this)

            schedulers[key.currencyCode] = scheduler
        }

        if (forceUpdate) {                                // make request for scheduler immediately
            schedulers[key.currencyCode]?.start(true)
        }

        return subject
            .doOnDispose {
                onDisposed(key)
            }
    }

    override fun getCoinTypes(currencyCode: String): List<CoinType> {
        return observingCoinTypes(currencyCode).toList()
    }

    fun getLatestRatesAsync(coinTypes: List<CoinType>, currencyCode: String): Observable<Map<CoinType, LatestRate>> {
        val key = LatestRateKey(coinTypes, currencyCode)

        return subject(key)
    }

    fun refresh(currencyCode: String) {
        schedulers[currencyCode]?.start(force = true)
    }

    fun getLatestRateAsync(key: PairKey): Observable<LatestRate> {
        val latestRateKey = LatestRateKey(listOf(key.coinType), key.currencyCode)
        return subject(latestRateKey)
            .flatMap { dictionary ->
                dictionary[key.coinType]?.let {
                    Observable.just(it)
                } ?: Observable.never()
            }
    }

    //  LatestRateManager.Listener

    override val coinTypes: Map<String, List<CoinType>>
        get() = subjects.map { it.key.currencyCode to it.key.coinTypes }.toMap()

    override fun onUpdate(latestRates: Map<CoinType, LatestRate>, currencyCode: String) {
        subjects.forEach { (key, subject) ->
            if (key.currencyCode == currencyCode) {
                val rates = latestRates.filter { (rateKey, _) ->
                    key.coinTypes.contains(rateKey)
                }
                if (rates.isNotEmpty()) {
                    subject.onNext(rates)
                }
            }
        }
    }
}
