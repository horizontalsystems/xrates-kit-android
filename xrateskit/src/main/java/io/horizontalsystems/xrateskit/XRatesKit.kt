package io.horizontalsystems.xrateskit

import android.content.Context
import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.chartpoint.ChartInfoManager
import io.horizontalsystems.xrateskit.chartpoint.ChartInfoSchedulerFactory
import io.horizontalsystems.xrateskit.chartpoint.ChartInfoSyncManager
import io.horizontalsystems.xrateskit.coinmarkets.CoinMarketsManager
import io.horizontalsystems.xrateskit.coinmarkets.DefiMarketsManager
import io.horizontalsystems.xrateskit.coinmarkets.GlobalMarketInfoManager
import io.horizontalsystems.xrateskit.coinmarkets.TokenInfoManager
import io.horizontalsystems.xrateskit.coins.CoinInfoManager
import io.horizontalsystems.xrateskit.coins.CoinSyncer
import io.horizontalsystems.xrateskit.coins.ProviderCoinsManager
import io.horizontalsystems.xrateskit.coins.provider.*
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.cryptonews.CryptoNewsManager
import io.horizontalsystems.xrateskit.entities.*
import io.horizontalsystems.xrateskit.providers.*
import io.horizontalsystems.xrateskit.providers.coingecko.CoinGeckoProvider
import io.horizontalsystems.xrateskit.providers.cryptocompare.CryptoCompareProvider
import io.horizontalsystems.xrateskit.providers.defiyield.DefiYieldProvider
import io.horizontalsystems.xrateskit.providers.horsys.HorsysProvider
import io.horizontalsystems.xrateskit.rates.HistoricalRateManager
import io.horizontalsystems.xrateskit.rates.LatestRatesManager
import io.horizontalsystems.xrateskit.rates.LatestRatesSchedulerFactory
import io.horizontalsystems.xrateskit.rates.LatestRatesSyncManager
import io.horizontalsystems.xrateskit.storage.Database
import io.horizontalsystems.xrateskit.storage.Storage
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
    private val defiMarketsManager: DefiMarketsManager,
    private val coinInfoManager: CoinInfoManager,
    private val providerCoinsManager: ProviderCoinsManager,
    private val tokenInfoManager: TokenInfoManager,

    coinSyncer: CoinSyncer
) {

    init {
        coinSyncer.sync()
    }

    fun getNotificationCoinCode(coinType: CoinType): String? {
        return providerCoinsManager.getProviderIds(listOf(coinType), InfoProvider.CryptoCompare()).firstOrNull()
    }

    fun refresh(currencyCode: String) {
        latestRatesSyncManager.refresh(currencyCode)
    }

    fun getLatestRate(coinType: CoinType, currencyCode: String): LatestRate? {
        return latestRatesManager.getLatestRate(coinType, currencyCode)
    }

    fun getLatestRateMap(coinTypes: List<CoinType>, currencyCode: String): Map<CoinType, LatestRate> {
        return latestRatesManager.getLatestRates(coinTypes, currencyCode)
    }

    fun getLatestRateAsync(coinType: CoinType, currencyCode: String): Observable<LatestRate> {
        return latestRatesSyncManager.getLatestRateAsync(PairKey(coinType, currencyCode))
    }

    fun latestRateMapObservable(coinTypes: List<CoinType>, currencyCode: String): Observable<Map<CoinType, LatestRate>> {
        return latestRatesSyncManager.getLatestRatesAsync(coinTypes, currencyCode)
    }

    fun getChartInfo(coinType: CoinType, currencyCode: String, chartType: ChartType): ChartInfo? {
        return chartInfoManager.getChartInfo(ChartInfoKey(coinType, currencyCode, chartType))
    }

    fun chartInfoObservable(coinType: CoinType, currencyCode: String, chartType: ChartType): Observable<ChartInfo> {
        return chartInfoSyncManager.chartInfoObservable(ChartInfoKey(coinType, currencyCode, chartType))
    }

    fun getHistoricalRate(coinType: CoinType, currencyCode: String, timestamp: Long): BigDecimal? {
        return historicalRateManager.getHistoricalRate(coinType, currencyCode, timestamp)
    }

    fun getHistoricalRateAsync(coinType: CoinType, currencyCode: String, timestamp: Long): Single<BigDecimal> {
        return historicalRateManager.getHistoricalRateAsync(coinType, currencyCode, timestamp)
    }

    fun cryptoNewsAsync(latestTimestamp: Long? = null): Single<List<CryptoNews>> {
        return cryptoNewsManager.getNewsAsync(latestTimestamp)
    }

    fun getTopTokenHoldersAsync(coinType: CoinType, itemsCount: Int = 20): Single<List<TokenHolder>> {
        return tokenInfoManager.getTopTokenHoldersAsync(coinType, itemsCount)
    }

    fun getAuditReportsAsync(coinType: CoinType): Single<List<Auditor>> {
        return tokenInfoManager.getAuditReportsAsync(coinType)
    }

    fun getTopCoinMarketsAsync(currencyCode: String, fetchDiffPeriod: TimePeriod = TimePeriod.HOUR_24, itemsCount: Int = 200): Single<List<CoinMarket>> {
        return coinMarketManager.getTopCoinMarketsAsync(currencyCode, fetchDiffPeriod, itemsCount)
    }

    fun getTopDefiMarketsAsync(currencyCode: String, fetchDiffPeriod: TimePeriod = TimePeriod.HOUR_24, itemsCount: Int = 200): Single<List<CoinMarket>> {
        return defiMarketsManager.getTopDefiMarketsAsync(currencyCode, fetchDiffPeriod, itemsCount)
    }

    fun getTopDefiTvlAsync(currencyCode: String, fetchDiffPeriod: TimePeriod = TimePeriod.HOUR_24, itemsCount: Int = 200, chain: String? = null): Single<List<DefiTvl>> {
        return defiMarketsManager.getTopDefiTvlAsync(currencyCode, fetchDiffPeriod, itemsCount, chain)
    }

    fun getDefiTvlPointsAsync(coinType: CoinType, currencyCode: String, fetchDiffPeriod: TimePeriod = TimePeriod.HOUR_24): Single<List<DefiTvlPoint>> {
        return defiMarketsManager.getDefiTvlPointsAsync(coinType, currencyCode, fetchDiffPeriod)
    }

    fun getCoinMarketPointsAsync(coinType: CoinType, currencyCode: String, fetchDiffPeriod: TimePeriod = TimePeriod.HOUR_24): Single<List<CoinMarketPoint>> {
        return coinMarketManager.getCoinMarketPointsAsync(coinType, currencyCode, fetchDiffPeriod)
    }

    fun getDefiTvlAsync(coinType: CoinType, currencyCode: String): Single<DefiTvl> {
        return defiMarketsManager.getDefiTvlAsync(coinType, currencyCode)
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

    fun getGlobalCoinMarketsAsync(currencyCode: String, timePeriod: TimePeriod = TimePeriod.HOUR_24): Single<GlobalCoinMarket> {
        return globalMarketInfoManager.getGlobalMarketInfo(currencyCode, timePeriod)
    }

    fun getGlobalCoinMarketPointsAsync(currencyCode: String, timePeriod: TimePeriod = TimePeriod.HOUR_24): Single<List<GlobalCoinMarketPoint>> {
        return globalMarketInfoManager.getGlobalMarketPoints(currencyCode, timePeriod)
    }

    fun searchCoins(searchText: String): List<CoinData> {
        return providerCoinsManager.searchCoins(searchText)
    }

    fun clear(){
        coinMarketManager.destroy()
        globalMarketInfoManager.destroy()
    }

    companion object {
        fun create(context: Context, rateExpirationInterval: Long = 60L, retryInterval: Long = 30, cryptoCompareApiKey: String = "", defiyieldProviderApiKey: String, coinsRemoteUrl: String?, providerCoinsRemoteUrl: String?): XRatesKit {
            val factory = Factory(rateExpirationInterval)
            val storage = Storage(Database.create(context))

            val coinInfoResourceProvider = buildCoinInfoResourceProvider(context, coinsRemoteUrl)
            val coinInfoSyncer = CoinInfoSyncer(coinInfoResourceProvider, storage)
            val coinInfoManager = CoinInfoManager(storage, coinInfoSyncer)

            val providerCoinsResourceProvider = buildProviderCoinsResourceProviderImpl(context, providerCoinsRemoteUrl)
            val coinExternalIdsSyncer = CoinExternalIdsSyncer(providerCoinsResourceProvider, storage)
            val providerCoinsManager = ProviderCoinsManager(storage, coinExternalIdsSyncer)

            val coinGeckoProvider = CoinGeckoProvider(factory, coinInfoManager, providerCoinsManager)
            providerCoinsManager.coinGeckoProvider = coinGeckoProvider
            val cryptoCompareProvider = CryptoCompareProvider(factory, cryptoCompareApiKey)
            val horsysProvider = HorsysProvider(providerCoinsManager)
            val defiYieldProvider = DefiYieldProvider(defiyieldProviderApiKey)
            val globalMarketInfoManager = GlobalMarketInfoManager(horsysProvider, storage)
            val defiMarketInfoManager = DefiMarketsManager(coinGeckoProvider, horsysProvider)

            val historicalRateManager = HistoricalRateManager(storage, coinGeckoProvider)
            val cryptoNewsManager = CryptoNewsManager(cryptoCompareProvider)

            val latestRatesManager = LatestRatesManager(storage, factory)
            val latestRatesSchedulerFactory = LatestRatesSchedulerFactory(latestRatesManager, coinGeckoProvider, rateExpirationInterval, retryInterval)
            val latestRatesSyncManager = LatestRatesSyncManager(latestRatesSchedulerFactory).also {
                latestRatesManager.listener = it
            }

            val chartInfoManager = ChartInfoManager(storage, factory)
            val chartInfoSchedulerFactory = ChartInfoSchedulerFactory(chartInfoManager, coinGeckoProvider, retryInterval)
            val chartInfoSyncManager = ChartInfoSyncManager(chartInfoSchedulerFactory).also {
                chartInfoManager.listener = it
            }

            val topMarketsManager = CoinMarketsManager(coinGeckoProvider, horsysProvider)
            val tokenInfoManager = TokenInfoManager(storage, horsysProvider, defiYieldProvider)

            val coinSyncer = CoinSyncer(providerCoinsManager, coinInfoManager)

            return XRatesKit(
                    latestRatesManager,
                    latestRatesSyncManager,
                    chartInfoManager,
                    chartInfoSyncManager,
                    historicalRateManager,
                    cryptoNewsManager,
                    topMarketsManager,
                    globalMarketInfoManager,
                    defiMarketInfoManager,
                    coinInfoManager,
                    providerCoinsManager,
                    tokenInfoManager,
                    coinSyncer
            )
        }

        private fun buildProviderCoinsResourceProviderImpl(context: Context, providerCoinsRemoteUrl: String?): DataProvider<ProviderCoinsResource> {
            val provider = DataProviderChain<ProviderCoinsResource>()
            provider.addProvider(DataProviderCoinExternalIdsLocal(context))
            providerCoinsRemoteUrl?.let {
                provider.addProvider(DataProviderCoinExternalIdsRemote(providerCoinsRemoteUrl))
            }

            return provider
        }

        private fun buildCoinInfoResourceProvider(context: Context, coinsRemoteUrl: String?): DataProvider<CoinInfoResource> {
            val provider = DataProviderChain<CoinInfoResource>()
            provider.addProvider(DataProviderCoinsInfoLocal(context))
            coinsRemoteUrl?.let {
                provider.addProvider(DataProviderCoinsInfoRemote(coinsRemoteUrl))
            }
            return provider
        }
    }
}
