package io.horizontalsystems.xrateskit

import android.content.Context
import io.horizontalsystems.xrateskit.api.ApiManager
import io.horizontalsystems.xrateskit.api.CryptoCompareProvider
import io.horizontalsystems.xrateskit.chartpoint.ChartPointManager
import io.horizontalsystems.xrateskit.chartpoint.ChartPointSchedulerFactory
import io.horizontalsystems.xrateskit.chartpoint.ChartPointSyncManager
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.entities.*
import io.horizontalsystems.xrateskit.managers.HistoricalRateManager
import io.horizontalsystems.xrateskit.marketinfo.MarketInfoManager
import io.horizontalsystems.xrateskit.marketinfo.MarketInfoSchedulerFactory
import io.horizontalsystems.xrateskit.marketinfo.MarketInfoSyncManager
import io.horizontalsystems.xrateskit.storage.Database
import io.horizontalsystems.xrateskit.storage.Storage
import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigDecimal

class XRatesKit(
        private val marketInfoManager: MarketInfoManager,
        private val marketInfoSyncManager: MarketInfoSyncManager,
        private val chartPointManager: ChartPointManager,
        private val chartPointSyncManager: ChartPointSyncManager,
        private val historicalRateManager: HistoricalRateManager) {

    fun set(coins: List<String>) {
        marketInfoSyncManager.set(coins)
    }

    fun set(currency: String) {
        marketInfoSyncManager.set(currency)
    }

    fun refresh() {
        marketInfoSyncManager.refresh()
    }

    fun getMarketInfo(coin: String, currency: String): MarketInfo? {
        return marketInfoManager.getMarketInfo(coin, currency)
    }

    fun marketInfoObservable(coin: String, currency: String): Observable<MarketInfo> {
        return marketInfoSyncManager.marketInfoObservable(MarketInfoKey(coin, currency))
    }

    fun marketInfoMapObservable(currency: String): Observable<Map<String, MarketInfo>> {
        return marketInfoSyncManager.marketInfoMapObservable(currency)
    }

    fun getChartInfo(coin: String, currency: String, chartType: ChartType): ChartInfo? {
        return chartPointManager.getChartInfo(ChartPointKey(coin, currency, chartType))
    }

    fun chartInfoObservable(coin: String, currency: String, chartType: ChartType): Observable<ChartInfo> {
        return chartPointSyncManager.chartPointsObservable(ChartPointKey(coin, currency, chartType))
    }

    fun historicalRate(coin: String, currency: String, timestamp: Long): Single<BigDecimal> {
        return historicalRateManager.getHistoricalRate(coin, currency, timestamp)
    }

    companion object {
        fun create(context: Context, currency: String, rateExpirationInterval: Long = 60L, retryInterval: Long = 10): XRatesKit {
            val factory = Factory(rateExpirationInterval)
            val storage = Storage(Database.create(context))

            val apiManager = ApiManager()
            val cryptoCompareProvider = CryptoCompareProvider(factory, apiManager, "https://min-api.cryptocompare.com")

            val historicalRateManager = HistoricalRateManager(storage, cryptoCompareProvider)

            val marketInfoManager = MarketInfoManager(storage, factory)
            val marketInfoSchedulerFactory = MarketInfoSchedulerFactory(marketInfoManager, cryptoCompareProvider, rateExpirationInterval, retryInterval)
            val marketInfoSyncManager = MarketInfoSyncManager(currency, marketInfoSchedulerFactory).also {
                marketInfoManager.listener = it
            }

            val chartPointManager = ChartPointManager(storage, factory, marketInfoManager)
            val chartPointSchedulerFactory = ChartPointSchedulerFactory(chartPointManager, cryptoCompareProvider, retryInterval)
            val chartPointSyncManager = ChartPointSyncManager(chartPointSchedulerFactory, chartPointManager, marketInfoSyncManager).also {
                chartPointManager.listener = it
            }

            return XRatesKit(
                    marketInfoManager,
                    marketInfoSyncManager,
                    chartPointManager,
                    chartPointSyncManager,
                    historicalRateManager
            )
        }
    }
}
