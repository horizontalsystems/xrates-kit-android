package io.horizontalsystems.xrateskit

import android.content.Context
import io.horizontalsystems.xrateskit.api.ApiManager
import io.horizontalsystems.xrateskit.api.CryptoCompareProvider
import io.horizontalsystems.xrateskit.core.*
import io.horizontalsystems.xrateskit.storage.Database
import io.horizontalsystems.xrateskit.storage.Rate
import io.horizontalsystems.xrateskit.storage.Storage
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject

class XRatesKit(private val storage: IStorage, private val dataSource: XRatesDataSource, private val latestRateScheduler: SyncScheduler)
    : LatestRateSyncer.Listener {

    val rateFlowable: Flowable<Rate>
        get() = rateSubject.toFlowable(BackpressureStrategy.BUFFER)

    private val rateSubject = PublishSubject.create<Rate>()

    fun start(coins: List<String>, currency: String) {
        dataSource.coins = coins
        dataSource.currency = currency

        latestRateScheduler.start()
    }

    fun latestRate(coin: String, currency: String): Rate? {
        return storage.getRate(coin, currency)
    }

    fun refresh() {
        latestRateScheduler.start()
    }

    fun stop() {
        latestRateScheduler.stop()
    }

    //  RateSyncer Listener

    override fun onUpdate(rate: Rate) {
        rateSubject.onNext(rate)
    }

    companion object {
        fun create(context: Context): XRatesKit {
            val storage = Storage(Database.create(context))
            val dataSource = XRatesDataSource()
            val factory = Factory()

            val apiManager = ApiManager()
            val latestRateProvider = LatestRateProviderChain()
            val latestRateScheduler = SyncScheduler(5 * 60, 60)
            val latestRateSyncer = LatestRateSyncer(storage, dataSource, latestRateProvider)

            val exchangeRatesKit = XRatesKit(storage, dataSource, latestRateScheduler)

            latestRateSyncer.listener = exchangeRatesKit
            latestRateSyncer.syncListener = latestRateScheduler

            latestRateScheduler.listener = latestRateSyncer
            latestRateProvider.add(CryptoCompareProvider(factory, apiManager, "https://min-api.cryptocompare.com"))

            return exchangeRatesKit
        }
    }
}
