package io.horizontalsystems.xrateskit.core

import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Single

interface IStorage {
    //  HistoricalRate
    fun saveHistoricalRate(rate: HistoricalRate)
    fun getHistoricalRate(coin: String, currency: String, timestamp: Long): HistoricalRate?

    //  ChartPoint
    fun getChartPoints(key: ChartInfoKey): List<ChartPointEntity>
    fun saveChartPoints(points: List<ChartPointEntity>)
    fun deleteChartPoints(key: ChartInfoKey)

    //  MarketInfo
    fun getMarketInfo(coin: String, currency: String): MarketInfoEntity?
    fun getOldMarketInfo(coins: List<String>, currency: String): List<MarketInfoEntity>
    fun saveMarketInfo(marketInfoList: List<MarketInfoEntity>)

    //  GlobalMarketInfo
    fun getGlobalMarketInfo(currency: String): GlobalMarketInfo?
    fun saveGlobalMarketInfo(globalMarketInfo: GlobalMarketInfo)

    // Top markets
    fun getTopMarketCoins(): List<TopMarketCoin>
    fun saveTopMarkets(topMarkets: List<TopMarket>)
}

interface IFiatXRatesProvider {
    fun getLatestFiatXRates(sourceCurrency: String, targetCurrency: String): Double
}

interface IMarketInfoProvider {
    fun getMarketInfo(coins: List<Coin>, currency: String): Single<List<MarketInfoEntity>>
}

interface IChartInfoProvider {
    fun getChartPoints(chartPointKey: ChartInfoKey): Single<List<ChartPointEntity>>
}

interface IHistoricalRateProvider {
    fun getHistoricalRate(coin: String, currency: String, timestamp: Long): Single<HistoricalRate>
}

interface ICryptoNewsProvider {
    fun getNews(categories: String): Single<List<CryptoNews>>
}

interface ITopMarketsProvider {
    fun getTopMarketsAsync(itemsCount: Int, currencyCode: String, fetchDiffPeriod: TimePeriod): Single<List<TopMarket>>
}

interface ITopDefiMarketsProvider: ITopMarketsProvider {
}

interface IGlobalMarketInfoProvider {
    fun getGlobalMarketInfoAsync(currency: String): Single<GlobalMarketInfo>
}

interface ICoinInfoProvider {
    fun getGlobalMarketInfo(currency: String): Single<GlobalMarketInfo>
}
