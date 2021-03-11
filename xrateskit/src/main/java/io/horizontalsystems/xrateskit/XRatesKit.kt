package io.horizontalsystems.xrateskit

import android.content.Context
import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.api.*
import io.horizontalsystems.xrateskit.api.graphproviders.UniswapGraphProvider
import io.horizontalsystems.xrateskit.chartpoint.ChartInfoManager
import io.horizontalsystems.xrateskit.chartpoint.ChartInfoSchedulerFactory
import io.horizontalsystems.xrateskit.chartpoint.ChartInfoSyncManager
import io.horizontalsystems.xrateskit.coins.CoinInfoManager
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.cryptonews.CryptoNewsManager
import io.horizontalsystems.xrateskit.entities.*
import io.horizontalsystems.xrateskit.rates.HistoricalRateManager
import io.horizontalsystems.xrateskit.rates.LatestRatesManager
import io.horizontalsystems.xrateskit.rates.LatestRatesSchedulerFactory
import io.horizontalsystems.xrateskit.rates.LatestRatesSyncManager
import io.horizontalsystems.xrateskit.storage.Database
import io.horizontalsystems.xrateskit.storage.Storage
import io.horizontalsystems.xrateskit.coinmarkets.GlobalMarketInfoManager
import io.horizontalsystems.xrateskit.coinmarkets.CoinMarketsManager
import io.horizontalsystems.xrateskit.coins.ProviderCoinsManager
import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigDecimal

class XRatesKit(
    private val latestRatesManager: LatestRatesManager,
    private val latestRatesSyncManager: LatestRatesSyncManager,
    private val chartInfoManager: ChartInfoManager,
    private val chartInfoSyncManager: ChartInfoSyncManager,
    private val historicalRateManager: HistoricalRateManager,
    private val cryptoNewsManager: CryptoNewsManager,
    private val coinMarketManager: CoinMarketsManager,
    private val globalMarketInfoManager: GlobalMarketInfoManager,
    private val coinInfoManager: CoinInfoManager,
    private val providerCoinsManager: ProviderCoinsManager
) {

    fun set(coins: List<CoinType>) {
        latestRatesSyncManager.set(coins)
    }

    fun set(currencyCode: String) {
        latestRatesSyncManager.set(currencyCode)
    }

    fun refresh() {
        latestRatesSyncManager.refresh()
    }

    fun getLatestRate(coinType: CoinType, currencyCode: String): LatestRate? {
        return latestRatesManager.getLatestRate(coinType, currencyCode)
    }

    fun getLatestRateAsync(coinType: CoinType, currencyCode: String): Observable<LatestRate> {
        return latestRatesSyncManager.getLatestRateAsync(LatestRateKey(coinType, currencyCode))
    }

    fun latestRateMapObservable(currencyCode: String): Observable<Map<CoinType, LatestRate>> {
        return latestRatesSyncManager.getLatestRateMapObservable(currencyCode)
    }

    fun getChartInfo(coinType: CoinType, currencyCode: String, chartType: ChartType): ChartInfo? {
        return chartInfoManager.getChartInfo(ChartInfoKey(coinType, currencyCode, chartType))
    }

    fun chartInfoObservable(coinType: CoinType, currencyCode: String, chartType: ChartType): Observable<ChartInfo> {
        return chartInfoSyncManager.chartInfoObservable(ChartInfoKey(coinType, currencyCode, chartType))
    }

    fun historicalRate(coinType: CoinType, currencyCode: String, timestamp: Long): BigDecimal? {
        return historicalRateManager.getHistoricalRate(coinType, currencyCode, timestamp)
    }

    fun historicalRateFromApi(coinType: CoinType, currencyCode: String, timestamp: Long): Single<BigDecimal> {
        return historicalRateManager.getHistoricalRateFromApi(coinType, currencyCode, timestamp)
    }

    fun cryptoNews(coinCode: String): Single<List<CryptoNews>> {
        return cryptoNewsManager.getNews(coinCode)
    }

    fun getTopCoinMarketsAsync(currencyCode: String, fetchDiffPeriod: TimePeriod = TimePeriod.HOUR_24, itemsCount: Int = 200): Single<List<CoinMarket>> {
        return coinMarketManager.getTopCoinMarketsAsync(currencyCode, fetchDiffPeriod, itemsCount)
    }

    fun getCoinMarketsAsync(coinTypes: List<CoinType>, currencyCode: String, fetchDiffPeriod: TimePeriod = TimePeriod.HOUR_24): Single<List<CoinMarket>> {
        return coinMarketManager.getCoinMarketsAsync(coinTypes , currencyCode, fetchDiffPeriod)
    }

    fun getCoinRatingsAsync(): Single<Map<CoinType, String>> {
        return coinInfoManager.getCoinRatingsAsync()
    }

    fun getCoinMarketsByCategoryAsync(categoryId: String, currencyCode: String, fetchDiffPeriod: TimePeriod = TimePeriod.HOUR_24): Single<List<CoinMarket>> {
        val coinCodes = coinInfoManager.getCoinCodesByCategory(categoryId)
        return coinMarketManager.getCoinMarketsAsync(coinCodes , currencyCode, fetchDiffPeriod)
    }

    fun getCoinMarketDetailsAsync(coinType: CoinType, currencyCode: String, rateDiffCoinCodes: List<String>, rateDiffPeriods: List<TimePeriod>): Single<CoinMarketDetails> {
        return coinMarketManager.getCoinMarketDetailsAsync(coinType, currencyCode, rateDiffCoinCodes, rateDiffPeriods)
    }

    fun getGlobalCoinMarketsAsync(currencyCode: String): Single<GlobalCoinMarket> {
        return globalMarketInfoManager.getGlobalMarketInfo(currencyCode)
    }

    fun searchCoins(searchText: String): List<CoinData> {
        return providerCoinsManager.searchCoins(searchText)
    }

    fun clear(){
        coinMarketManager.destroy()
        globalMarketInfoManager.destroy()
    }

    companion object {
        fun create(context: Context, currency: String, rateExpirationInterval: Long = 60L, retryInterval: Long = 30, indicatorPointCount: Int = 50, cryptoCompareApiKey: String = ""): XRatesKit {
            val factory = Factory(rateExpirationInterval)
            val storage = Storage(Database.create(context))
            val coinInfoManager = CoinInfoManager(context, storage)
            val providerCoinsManager = ProviderCoinsManager(context, storage)

            val apiManager = ApiManager()
            val coinPaprikaProvider = CoinPaprikaProvider(apiManager)
            val horsysProvider = HorsysProvider(apiManager)
            val coinGeckoProvider = CoinGeckoProvider(factory, apiManager, coinInfoManager, providerCoinsManager)
            val cryptoCompareProvider = CryptoCompareProvider(factory, apiManager, cryptoCompareApiKey, indicatorPointCount, providerCoinsManager)
            val uniswapGraphProvider = UniswapGraphProvider(factory, apiManager, cryptoCompareProvider)
            val latestRatesProvider = BaseLatestRateProvider(cryptoCompareProvider, uniswapGraphProvider)
            val chartInfoProvider = BaseChartInfoProvider(providerCoinsManager, cryptoCompareProvider, coinGeckoProvider)
            val globalMarketInfoManager = GlobalMarketInfoManager(coinPaprikaProvider, horsysProvider, storage)

            val historicalRateManager = HistoricalRateManager(storage, cryptoCompareProvider)
            val cryptoNewsManager = CryptoNewsManager(30, cryptoCompareProvider)

            val latestRatesManager = LatestRatesManager(storage, factory)
            val latestRatesSchedulerFactory = LatestRatesSchedulerFactory(latestRatesManager, latestRatesProvider, rateExpirationInterval, retryInterval)
            val latestRatesSyncManager = LatestRatesSyncManager(currency, latestRatesSchedulerFactory).also {
                latestRatesManager.listener = it
            }

            val chartInfoManager = ChartInfoManager(storage, factory, latestRatesManager)
            val chartInfoSchedulerFactory = ChartInfoSchedulerFactory(chartInfoManager, chartInfoProvider, retryInterval)
            val chartInfoSyncManager = ChartInfoSyncManager(chartInfoSchedulerFactory).also {
                chartInfoManager.listener = it
            }

            val topMarketsManager = CoinMarketsManager(coinGeckoProvider, storage, factory)

            return XRatesKit(
                    latestRatesManager,
                    latestRatesSyncManager,
                    chartInfoManager,
                    chartInfoSyncManager,
                    historicalRateManager,
                    cryptoNewsManager,
                    topMarketsManager,
                    globalMarketInfoManager,
                    coinInfoManager,
                    providerCoinsManager
            )
        }
    }
}
