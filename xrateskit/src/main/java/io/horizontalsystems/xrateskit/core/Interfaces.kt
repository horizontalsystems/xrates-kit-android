package io.horizontalsystems.xrateskit.core

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.api.InfoProvider
import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Single

interface IStorage {

    //ResourceInfo
    fun getResourceInfo(resourceType: ResourceType): ResourceInfo?
    fun saveResourceInfo(resourceInfo: ResourceInfo)

        // coinInfo
    fun getCoinInfoCount(): Int
    fun getCoinInfos(): List<CoinInfoEntity>
    fun getCoinCategories(coinType: CoinType): List<CoinCategory>
    fun getCoinInfosByCategory(categoryId: String): List<CoinInfoEntity>
    fun getCoinInfo(coinType: CoinType): CoinInfoEntity?
    fun getCoinLinks(coinType: CoinType): List<CoinLinksEntity>
    fun saveCoinInfos(coinInfos: List<CoinInfoEntity>)
    fun saveCoinCategories(coinCategoryEntities: List<CoinCategoriesEntity>)
    fun saveCoinCategory(coinCategories: List<CoinCategory>)
    fun saveCoinLinks(coinLinks: List<CoinLinksEntity>)
    fun deleteAllCoinCategories()
    fun deleteAllCoinsCategories()
    fun deleteAllCoinLinks()

        //Provider Coins
    fun saveProviderCoins(providerCoins: List<ProviderCoinEntity>)
    fun getProviderCoins(coinTypes: List<CoinType>): List<ProviderCoinEntity>
    fun getCoinTypesByProviderCoinId(providerCoinId: String, provider: InfoProvider): List<CoinType>
    fun searchCoins(searchText: String): List<ProviderCoinEntity>

    //  HistoricalRate
    fun saveHistoricalRate(rate: HistoricalRate)
    fun getHistoricalRate(coinType: CoinType, currencyCode: String, timestamp: Long): HistoricalRate?

    //  ChartPoint
    fun getChartPoints(key: ChartInfoKey): List<ChartPointEntity>
    fun saveChartPoints(points: List<ChartPointEntity>)
    fun deleteChartPoints(key: ChartInfoKey)

    //  MarketInfo
    fun getLatestRate(coinType: CoinType, currencyCode: String): LatestRateEntity?
    fun getOldLatestRates(coinTypes: List<CoinType>, currencyCode: String): List<LatestRateEntity>
    fun saveLatestRates(marketInfoList: List<LatestRateEntity>)

    //  GlobalMarketInfo
    fun getGlobalMarketInfo(currency: String): GlobalCoinMarket?
    fun saveGlobalMarketInfo(globalCoinMarket: GlobalCoinMarket)

}

interface IInfoManager {
    fun destroy()
}

interface ICoinMarketManager {
    fun getTopCoinMarketsAsync(currency: String, fetchDiffPeriod: TimePeriod, itemsCount: Int): Single<List<CoinMarket>>
    fun getCoinMarketsAsync(coinTypes: List<CoinType>, currencyCode: String, fetchDiffPeriod: TimePeriod): Single<List<CoinMarket>>
    fun getCoinMarketDetailsAsync(coinType: CoinType, currencyCode: String, rateDiffCoinCodes: List<String>, coinDiffPeriods: List<TimePeriod>): Single<CoinMarketDetails>
}

interface IInfoProvider {
    val provider: InfoProvider
    fun initProvider()
    fun destroy()
}

interface IFiatXRatesProvider {
    fun getLatestFiatXRates(sourceCurrency: String, targetCurrency: String): Double
}

interface ILatestRateProvider {
    fun getLatestRate(coinTypes: List<CoinType>, currency: String): Single<List<LatestRateEntity>>
}

interface IChartInfoProvider {
    fun getChartPoints(chartPointKey: ChartInfoKey): Single<List<ChartPointEntity>>
}

interface IHistoricalRateProvider {
    fun getHistoricalRate(coinType: CoinType, currency: String, timestamp: Long): Single<HistoricalRate>
}

interface ICryptoNewsProvider {
    fun getNews(categories: String): Single<List<CryptoNews>>
}

interface ICoinMarketProvider : IInfoProvider {
    fun getTopCoinMarketsAsync(currencyCode: String, fetchDiffPeriod: TimePeriod, itemsCount: Int): Single<List<CoinMarket>>
    fun getCoinMarketsAsync(coinTypes: List<CoinType>, currencyCode: String, fetchDiffPeriod: TimePeriod): Single<List<CoinMarket>>
    fun getCoinMarketDetailsAsync(coinType: CoinType, currencyCode: String, rateDiffCoinIds: List<String>, rateDiffPeriods: List<TimePeriod>): Single<CoinMarketDetails>
}

interface IGlobalCoinMarketProvider : IInfoProvider {
    fun getGlobalCoinMarketsAsync(currency: String): Single<GlobalCoinMarket>
}