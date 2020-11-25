package io.horizontalsystems.xrateskit

import android.content.Context
import io.horizontalsystems.xrateskit.api.ApiManager
import io.horizontalsystems.xrateskit.api.CoinMarketCapProvider
import io.horizontalsystems.xrateskit.api.CryptoCompareProvider
import io.horizontalsystems.xrateskit.api.MarketInfoBaseProvider
import io.horizontalsystems.xrateskit.api.uniswapgraph.UniswapGraphProvider
import io.horizontalsystems.xrateskit.chartpoint.ChartInfoManager
import io.horizontalsystems.xrateskit.chartpoint.ChartInfoSchedulerFactory
import io.horizontalsystems.xrateskit.chartpoint.ChartInfoSyncManager
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.ITopMarketsProvider
import io.horizontalsystems.xrateskit.cryptonews.CryptoNewsManager
import io.horizontalsystems.xrateskit.entities.*
import io.horizontalsystems.xrateskit.managers.HistoricalRateManager
import io.horizontalsystems.xrateskit.marketinfo.MarketInfoManager
import io.horizontalsystems.xrateskit.marketinfo.MarketInfoSchedulerFactory
import io.horizontalsystems.xrateskit.marketinfo.MarketInfoSyncManager
import io.horizontalsystems.xrateskit.storage.Database
import io.horizontalsystems.xrateskit.storage.Storage
import io.horizontalsystems.xrateskit.toplist.TopMarketsManager
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
        private val topMarketsManager: TopMarketsManager) {

    fun set(coins: List<Coin>) {
        marketInfoSyncManager.set(coins)
    }

    fun set(currencyCode: String) {
        marketInfoSyncManager.set(currencyCode)
    }

    fun refresh() {
        marketInfoSyncManager.refresh()
    }

    fun getMarketInfo(coinCode: String, currencyCode: String): MarketInfo? {
        return marketInfoManager.getMarketInfo(coinCode, currencyCode)
    }

    fun marketInfoObservable(coinCode: String, currencyCode: String): Observable<MarketInfo> {
        return marketInfoSyncManager.marketInfoObservable(MarketInfoKey(coinCode, currencyCode))
    }

    fun marketInfoMapObservable(currencyCode: String): Observable<Map<String, MarketInfo>> {
        return marketInfoSyncManager.marketInfoMapObservable(currencyCode)
    }

    fun getChartInfo(coinCode: String, currencyCode: String, chartType: ChartType): ChartInfo? {
        return chartInfoManager.getChartInfo(ChartInfoKey(coinCode, currencyCode, chartType))
    }

    fun chartInfoObservable(coinCode: String, currencyCode: String, chartType: ChartType): Observable<ChartInfo> {
        return chartInfoSyncManager.chartInfoObservable(ChartInfoKey(coinCode, currencyCode, chartType))
    }

    fun historicalRate(coinCode: String, currencyCode: String, timestamp: Long): BigDecimal? {
        return historicalRateManager.getHistoricalRate(coinCode, currencyCode, timestamp)
    }

    fun historicalRateFromApi(coinCode: String, currencyCode: String, timestamp: Long): Single<BigDecimal> {
        return historicalRateManager.getHistoricalRateFromApi(coinCode, currencyCode, timestamp)
    }

    fun cryptoNews(coinCode: String): Single<List<CryptoNews>> {
        return cryptoNewsManager.getNews(coinCode)
    }

    fun getTopMarkets(currencyCode: String): Single<List<TopMarket>> {
        return topMarketsManager.getTopMarkets(currencyCode)
    }

    companion object {
        fun create(context: Context, currency: String, rateExpirationInterval: Long = 60L, retryInterval: Long = 30, topMarketsCount: Int = 100, indicatorPointCount: Int = 50, cryptoCompareApiKey: String = "", coinMarketCapApiKey: String = ""): XRatesKit {
            val factory = Factory(rateExpirationInterval)
            val storage = Storage(Database.create(context))

            val apiManager = ApiManager()
            val cryptoCompareProvider = CryptoCompareProvider(factory, apiManager, "https://min-api.cryptocompare.com", cryptoCompareApiKey, topMarketsCount, indicatorPointCount)
            val uniswapGraphProvider = UniswapGraphProvider(factory, apiManager, cryptoCompareProvider)
            val marketInfoProvider = MarketInfoBaseProvider(cryptoCompareProvider, uniswapGraphProvider)

            val historicalRateManager = HistoricalRateManager(storage, cryptoCompareProvider)
            val cryptoNewsManager = CryptoNewsManager(30, cryptoCompareProvider)

            val marketInfoManager = MarketInfoManager(storage, factory)
            val marketInfoSchedulerFactory = MarketInfoSchedulerFactory(marketInfoManager, marketInfoProvider, rateExpirationInterval, retryInterval)
            val marketInfoSyncManager = MarketInfoSyncManager(currency, marketInfoSchedulerFactory).also {
                marketInfoManager.listener = it
            }

            val chartInfoManager = ChartInfoManager(storage, factory, marketInfoManager)
            val chartInfoSchedulerFactory = ChartInfoSchedulerFactory(chartInfoManager, cryptoCompareProvider, retryInterval)
            val chartInfoSyncManager = ChartInfoSyncManager(chartInfoSchedulerFactory).also {
                chartInfoManager.listener = it
            }

            val topMarketsProvider: ITopMarketsProvider = if (coinMarketCapApiKey.isNotBlank()) {
                CoinMarketCapProvider(factory, apiManager, "https://pro-api.coinmarketcap.com/v1/cryptocurrency", topMarketsCount, coinMarketCapApiKey, marketInfoProvider)
            } else {
                cryptoCompareProvider
            }
            val topMarketsManager = TopMarketsManager(topMarketsProvider, factory, storage)

            return XRatesKit(
                    marketInfoManager,
                    marketInfoSyncManager,
                    chartInfoManager,
                    chartInfoSyncManager,
                    historicalRateManager,
                    cryptoNewsManager,
                    topMarketsManager
            )
        }
    }
}
