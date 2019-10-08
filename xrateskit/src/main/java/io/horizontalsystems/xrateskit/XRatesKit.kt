package io.horizontalsystems.xrateskit

import android.content.Context
import io.horizontalsystems.xrateskit.api.ApiManager
import io.horizontalsystems.xrateskit.api.CryptoCompareProvider
import io.horizontalsystems.xrateskit.core.*
import io.horizontalsystems.xrateskit.storage.Database
import io.horizontalsystems.xrateskit.storage.RateInfo
import io.horizontalsystems.xrateskit.storage.Storage
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.subjects.PublishSubject
import java.math.BigDecimal

class XRatesKit(
        private val storage: IStorage,
        private val dataSource: XRatesDataSource,
        private val latestRateScheduler: SyncScheduler,
        private val historicalRateManager: HistoricalRateManager)
    : LatestRateSyncer.Listener {

    val rateFlowable: Flowable<RateInfo>
        get() = rateSubject.toFlowable(BackpressureStrategy.BUFFER)

    private val rateSubject = PublishSubject.create<RateInfo>()

    fun start(coins: List<String>, currency: String) {
        dataSource.coins = coins
        dataSource.currency = currency

        latestRateScheduler.start()
    }

    fun getHistoricalRate(coin: String, currency: String, timestamp: Long): Single<BigDecimal> {
        return historicalRateManager.getHistoricalRate(coin, currency, timestamp)
    }

    fun getLatestRate(coin: String, currency: String): RateInfo? {
        return storage.getLatestRate(coin, currency)?.let { RateInfo(it.coin, it.currency, it.value, it.timestamp) }
    }

    fun refresh() {
        latestRateScheduler.start()
    }

    fun stop() {
        latestRateScheduler.stop()
    }

    //  RateSyncer Listener

    override fun onUpdate(rate: RateInfo) {
        rateSubject.onNext(rate)
    }

    companion object {
        fun create(context: Context): XRatesKit {
            val storage = Storage(Database.create(context))
            val dataSource = XRatesDataSource()
            val factory = Factory()

            val apiManager = ApiManager()
            val cryptoCompareProvider = CryptoCompareProvider(factory, apiManager, "https://min-api.cryptocompare.com")
            val latestRateScheduler = SyncScheduler(5 * 60, 60)
            val latestRateSyncer = LatestRateSyncer(storage, factory, dataSource, cryptoCompareProvider)
            val historicalRateManager = HistoricalRateManager(storage, cryptoCompareProvider)

            val exchangeRatesKit = XRatesKit(storage, dataSource, latestRateScheduler, historicalRateManager)

            latestRateSyncer.listener = exchangeRatesKit
            latestRateSyncer.syncListener = latestRateScheduler

            latestRateScheduler.listener = latestRateSyncer

            return exchangeRatesKit
        }
    }
}
