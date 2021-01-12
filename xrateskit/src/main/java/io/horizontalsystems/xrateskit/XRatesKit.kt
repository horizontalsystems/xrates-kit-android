package io.horizontalsystems.xrateskit

import android.content.Context
import io.horizontalsystems.xrateskit.api.*
import io.horizontalsystems.xrateskit.api.graphproviders.UniswapGraphProvider
import io.horizontalsystems.xrateskit.chartpoint.ChartInfoManager
import io.horizontalsystems.xrateskit.chartpoint.ChartInfoSchedulerFactory
import io.horizontalsystems.xrateskit.chartpoint.ChartInfoSyncManager
import io.horizontalsystems.xrateskit.coins.CoinInfoManager
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.cryptonews.CryptoNewsManager
import io.horizontalsystems.xrateskit.entities.*
import io.horizontalsystems.xrateskit.managers.HistoricalRateManager
import io.horizontalsystems.xrateskit.marketinfo.MarketInfoManager
import io.horizontalsystems.xrateskit.marketinfo.MarketInfoSchedulerFactory
import io.horizontalsystems.xrateskit.marketinfo.MarketInfoSyncManager
import io.horizontalsystems.xrateskit.storage.Database
import io.horizontalsystems.xrateskit.storage.Storage
import io.horizontalsystems.xrateskit.coinmarkets.GlobalMarketInfoManager
import io.horizontalsystems.xrateskit.coinmarkets.CoinMarketManager
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
        private val coinInfoManager: CoinInfoManager,
        private val coinMarketManager: CoinMarketManager,
        private val globalMarketInfoManager: GlobalMarketInfoManager
) {

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

    fun getTopCoinMarketsAsync(currencyCode: String, fetchDiffPeriod: TimePeriod = TimePeriod.HOUR_24, itemsCount: Int = 200): Single<List<CoinMarket>> {
        return coinMarketManager.getTopCoinMarketsAsync(currencyCode, fetchDiffPeriod, itemsCount)
    }

    fun getCoinMarketsAsync(coins: List<Coin>, currencyCode: String, fetchDiffPeriod: TimePeriod = TimePeriod.HOUR_24): Single<List<CoinMarket>> {
        return coinMarketManager.getCoinMarketsAsync(coins , currencyCode, fetchDiffPeriod)
    }

    fun getTopDefiMarketsAsync(currencyCode: String, fetchDiffPeriod: TimePeriod = TimePeriod.HOUR_24, itemsCount: Int = 200): Single<List<CoinMarket>> {
        return coinMarketManager.getTopDefiMarketsAsync(currencyCode, fetchDiffPeriod, itemsCount)
    }

    fun getGlobalCoinMarketsAsync(currencyCode: String): Single<GlobalCoinMarket> {
        return globalMarketInfoManager.getGlobalMarketInfo(currencyCode)
    }

    fun clear(){
        coinMarketManager.destroy()
        coinInfoManager.destroy()
        globalMarketInfoManager.destroy()
    }

    companion object {
        fun create(context: Context, currency: String, rateExpirationInterval: Long = 60L, retryInterval: Long = 30, indicatorPointCount: Int = 50, cryptoCompareApiKey: String = ""): XRatesKit {
            val factory = Factory(rateExpirationInterval)
            val storage = Storage(Database.create(context))

            val apiManager = ApiManager()
            val cryptoCompareProvider = CryptoCompareProvider(factory, apiManager, cryptoCompareApiKey, indicatorPointCount)
            val uniswapGraphProvider = UniswapGraphProvider(factory, apiManager, cryptoCompareProvider)
            val marketInfoProvider = BaseMarketInfoProvider(cryptoCompareProvider, uniswapGraphProvider)
            val coinPaprikaProvider = CoinPaprikaProvider(apiManager)
            val globalMarketInfoManager = GlobalMarketInfoManager(coinPaprikaProvider, storage)

            val coinInfoManager = CoinInfoManager(coinPaprikaProvider, storage)
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

            val topMarketsProvider = CoinGeckoProvider(factory,coinInfoManager, apiManager)
            val topDefiMarketsProvider = uniswapGraphProvider
            val topMarketsManager = CoinMarketManager(topMarketsProvider, topDefiMarketsProvider, coinInfoManager)

            return XRatesKit(
                    marketInfoManager,
                    marketInfoSyncManager,
                    chartInfoManager,
                    chartInfoSyncManager,
                    historicalRateManager,
                    cryptoNewsManager,
                    coinInfoManager,
                    topMarketsManager,
                    globalMarketInfoManager
            )
        }
    }
}
