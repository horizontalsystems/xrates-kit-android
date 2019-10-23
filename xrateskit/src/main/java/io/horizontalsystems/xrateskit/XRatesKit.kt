package io.horizontalsystems.xrateskit

import android.content.Context
import io.horizontalsystems.xrateskit.api.ApiManager
import io.horizontalsystems.xrateskit.api.CryptoCompareProvider
import io.horizontalsystems.xrateskit.chartpoint.ChartPointManager
import io.horizontalsystems.xrateskit.chartpoint.ChartPointSchedulerFactory
import io.horizontalsystems.xrateskit.chartpoint.ChartPointSyncManager
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.entities.*
import io.horizontalsystems.xrateskit.latestrate.LatestRateManager
import io.horizontalsystems.xrateskit.latestrate.LatestRateSchedulerFactory
import io.horizontalsystems.xrateskit.latestrate.LatestRateSyncManager
import io.horizontalsystems.xrateskit.managers.HistoricalRateManager
import io.horizontalsystems.xrateskit.managers.MarketStatsManager
import io.horizontalsystems.xrateskit.storage.Database
import io.horizontalsystems.xrateskit.storage.Storage
import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigDecimal

class XRatesKit(
        private val latestRateManager: LatestRateManager,
        private val latestRateSyncManager: LatestRateSyncManager,
        private val chartPointManager: ChartPointManager,
        private val chartPointSyncManager: ChartPointSyncManager,
        private val marketStatsManager: MarketStatsManager,
        private val historicalRateManager: HistoricalRateManager) {

    fun set(coins: List<String>) {
        latestRateSyncManager.set(coins)
    }

    fun set(currency: String) {
        latestRateSyncManager.set(currency)
    }

    fun refresh() {
        latestRateSyncManager.refresh()
    }

    fun getLatestRate(coin: String, currency: String): Rate? {
        return latestRateManager.getLatestRate(coin, currency)
    }

    fun latestRateObservable(coin: String, currency: String): Observable<Rate> {
        return latestRateSyncManager.latestRateObservable(LatestRateKey(coin, currency))
    }

    fun getChartInfo(coin: String, currency: String, chartType: ChartType): ChartInfo? {
        return chartPointManager.getChartInfo(ChartPointKey(coin, currency, chartType))
    }

    fun chartPointsObservable(coin: String, currency: String, chartType: ChartType): Observable<ChartInfo> {
        return chartPointSyncManager.chartPointsObservable(ChartPointKey(coin, currency, chartType))
    }

    fun historicalRateSingle(coin: String, currency: String, timestamp: Long): Single<BigDecimal> {
        return historicalRateManager.getHistoricalRate(coin, currency, timestamp)
    }

    fun marketStatsSingle(coin: String, currency: String): Single<MarketStatsInfo> {
        return marketStatsManager.getMarketStats(coin, currency)
    }

    companion object {
        fun create(context: Context, currency: String, rateExpirationInterval: Long = 60L, retryInterval: Long = 10): XRatesKit {
            val factory = Factory(rateExpirationInterval)
            val storage = Storage(Database.create(context))

            val apiManager = ApiManager()
            val cryptoCompareProvider = CryptoCompareProvider(factory, apiManager, "https://min-api.cryptocompare.com")

            val historicalRateManager = HistoricalRateManager(storage, cryptoCompareProvider)
            val marketStatsManager = MarketStatsManager(storage, factory, cryptoCompareProvider)

            val latestRateManager = LatestRateManager(storage, factory)
            val latestRateSchedulerFactory = LatestRateSchedulerFactory(factory, latestRateManager, cryptoCompareProvider, rateExpirationInterval, retryInterval)
            val latestRateSyncManager = LatestRateSyncManager(currency, latestRateSchedulerFactory).also {
                latestRateManager.listener = it
            }

            val chartPointManager = ChartPointManager(storage, factory, latestRateManager)
            val chartPointSchedulerFactory = ChartPointSchedulerFactory(chartPointManager, cryptoCompareProvider, retryInterval)
            val chartPointSyncManager = ChartPointSyncManager(chartPointSchedulerFactory, chartPointManager, latestRateSyncManager).also {
                chartPointManager.listener = it
            }

            return XRatesKit(
                    latestRateManager,
                    latestRateSyncManager,
                    chartPointManager,
                    chartPointSyncManager,
                    marketStatsManager,
                    historicalRateManager
            )
        }
    }
}
