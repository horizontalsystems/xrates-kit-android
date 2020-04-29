package io.horizontalsystems.xrateskit

import android.content.Context
import io.horizontalsystems.xrateskit.api.ApiManager
import io.horizontalsystems.xrateskit.api.CryptoCompareProvider
import io.horizontalsystems.xrateskit.chartpoint.ChartInfoManager
import io.horizontalsystems.xrateskit.chartpoint.ChartInfoSchedulerFactory
import io.horizontalsystems.xrateskit.chartpoint.ChartInfoSyncManager
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.cryptonews.CryptoNewsManager
import io.horizontalsystems.xrateskit.entities.*
import io.horizontalsystems.xrateskit.managers.HistoricalRateManager
import io.horizontalsystems.xrateskit.marketinfo.MarketInfoManager
import io.horizontalsystems.xrateskit.marketinfo.MarketInfoSchedulerFactory
import io.horizontalsystems.xrateskit.marketinfo.MarketInfoSyncManager
import io.horizontalsystems.xrateskit.storage.Database
import io.horizontalsystems.xrateskit.storage.Storage
import io.horizontalsystems.xrateskit.toplist.TopListManager
import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigDecimal

class XRatesKit(
        private val marketInfoManager: MarketInfoManager,
        private val marketInfoSyncManager: MarketInfoSyncManager,
        private val chartInfoManager: ChartInfoManager,
        private val chartInfoSyncManager: ChartInfoSyncManager,
        private val historicalRateManager: HistoricalRateManager,
        private val cryptoNewsManager: CryptoNewsManager,
        private val topListManager: TopListManager) {

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
        return chartInfoManager.getChartInfo(ChartInfoKey(coin, currency, chartType))
    }

    fun chartInfoObservable(coin: String, currency: String, chartType: ChartType): Observable<ChartInfo> {
        return chartInfoSyncManager.chartInfoObservable(ChartInfoKey(coin, currency, chartType))
    }

    fun historicalRate(coin: String, currency: String, timestamp: Long): BigDecimal? {
        return historicalRateManager.getHistoricalRate(coin, currency, timestamp)
    }

    fun historicalRateFromApi(coin: String, currency: String, timestamp: Long): Single<BigDecimal> {
        return historicalRateManager.getHistoricalRateFromApi(coin, currency, timestamp)
    }

    fun cryptoNews(coinCode: String): Single<List<CryptoNews>> {
        return cryptoNewsManager.getNews(coinCode)
    }

    fun getTopList(currency: String): Single<List<TopMarket>>{
        return topListManager.getTopList(currency)
    }

    companion object {
        fun create(context: Context, currency: String, rateExpirationInterval: Long = 60L, retryInterval: Long = 30): XRatesKit {
            val factory = Factory(rateExpirationInterval)
            val storage = Storage(Database.create(context))

            val apiManager = ApiManager()
            val cryptoCompareProvider = CryptoCompareProvider(factory, apiManager, "https://min-api.cryptocompare.com")

            val historicalRateManager = HistoricalRateManager(storage, cryptoCompareProvider)
            val cryptoNewsManager = CryptoNewsManager(30, cryptoCompareProvider)

            val marketInfoManager = MarketInfoManager(storage, factory)
            val marketInfoSchedulerFactory = MarketInfoSchedulerFactory(marketInfoManager, cryptoCompareProvider, rateExpirationInterval, retryInterval)
            val marketInfoSyncManager = MarketInfoSyncManager(currency, marketInfoSchedulerFactory).also {
                marketInfoManager.listener = it
            }

            val topListManager = TopListManager(cryptoCompareProvider, factory, marketInfoManager)

            val chartInfoManager = ChartInfoManager(storage, factory, marketInfoManager)
            val chartInfoSchedulerFactory = ChartInfoSchedulerFactory(chartInfoManager, cryptoCompareProvider, retryInterval)
            val chartInfoSyncManager = ChartInfoSyncManager(chartInfoSchedulerFactory, chartInfoManager, marketInfoSyncManager).also {
                chartInfoManager.listener = it
            }

            return XRatesKit(
                    marketInfoManager,
                    marketInfoSyncManager,
                    chartInfoManager,
                    chartInfoSyncManager,
                    historicalRateManager,
                    cryptoNewsManager,
                    topListManager
            )
        }
    }
}
