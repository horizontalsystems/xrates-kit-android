package io.horizontalsystems.xrateskit

import android.content.Context
import io.horizontalsystems.xrateskit.api.ApiManager
import io.horizontalsystems.xrateskit.api.CryptoCompareProvider
import io.horizontalsystems.xrateskit.core.*
import io.horizontalsystems.xrateskit.entities.ChartPoint
import io.horizontalsystems.xrateskit.entities.ChartType
import io.horizontalsystems.xrateskit.entities.MarketStatsInfo
import io.horizontalsystems.xrateskit.entities.RateInfo
import io.horizontalsystems.xrateskit.managers.ChartStatsSyncer
import io.horizontalsystems.xrateskit.managers.HistoricalRateManager
import io.horizontalsystems.xrateskit.managers.LatestRateSyncer
import io.horizontalsystems.xrateskit.managers.MarketStatsManager
import io.horizontalsystems.xrateskit.storage.Database
import io.horizontalsystems.xrateskit.storage.Storage
import io.reactivex.Flowable
import io.reactivex.Single
import java.math.BigDecimal

class XRatesKit(
        private val dataSource: XRatesDataSource,
        private val subjectHolder: SubjectHolder,
        private val syncScheduler: LatestRateScheduler,
        private val dataProvider: DataProvider) {

    fun set(coins: List<String>) {
        dataSource.coins = coins
        subjectHolder.clear()

        syncScheduler.start()
    }

    fun set(currency: String) {
        dataSource.currency = currency
        syncScheduler.start()
    }

    fun refresh() {
        syncScheduler.start(force = true)
    }

    fun getHistoricalRate(coin: String, currency: String, timestamp: Long): Single<BigDecimal> {
        return dataProvider.getHistoricalRate(coin, currency, timestamp)
    }

    fun getLatestRate(coin: String, currency: String): RateInfo? {
        return dataProvider.getLatestRate(coin, currency)
    }

    fun getMarketStats(coin: String, currency: String): Single<MarketStatsInfo> {
        return dataProvider.getMarketStats(coin, currency)
    }

    fun getChartStats(coin: String, currency: String, chartType: ChartType): List<ChartPoint> {
        return dataProvider.getChartPoints(coin, currency, chartType)
    }

    fun latestRateFlowable(coin: String, currency: String): Flowable<RateInfo> {
        return subjectHolder.latestRateFlowable(coin, currency)
    }

    fun chartStatsFlowable(coin: String, currency: String, chartType: ChartType): Flowable<List<ChartPoint>> {
        return subjectHolder.chartStatsFlowable(coin, currency, chartType)
    }

    companion object {
        fun create(context: Context, currency: String): XRatesKit {
            val rateExpirationSeconds = 20L
            val rateSyncRetrySeconds = 10L

            val factory = Factory(rateExpirationSeconds)
            val storage = Storage(Database.create(context))
            val dataSource = XRatesDataSource(currency = currency)

            val subjectHolder = SubjectHolder()

            val apiManager = ApiManager()
            val cryptoCompareProvider = CryptoCompareProvider(factory, apiManager, "https://min-api.cryptocompare.com")

            val chartStatsSyncer = ChartStatsSyncer(factory, storage, dataSource, subjectHolder, cryptoCompareProvider)
            val latestRateSyncer = LatestRateSyncer(factory, storage, dataSource, cryptoCompareProvider)

            val historicalRateManager = HistoricalRateManager(storage, cryptoCompareProvider)
            val marketStatsManager = MarketStatsManager(storage, cryptoCompareProvider)

            val dataProvider = DataProvider(storage, factory, subjectHolder, chartStatsSyncer, historicalRateManager, marketStatsManager)
            latestRateSyncer.listener = dataProvider

            val latestRateScheduler = LatestRateScheduler(rateExpirationSeconds, rateSyncRetrySeconds, latestRateSyncer)

            return XRatesKit(dataSource, subjectHolder, latestRateScheduler, dataProvider)
        }
    }
}
