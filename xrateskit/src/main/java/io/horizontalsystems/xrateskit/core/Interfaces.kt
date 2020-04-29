package io.horizontalsystems.xrateskit.core

import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Single

interface IStorage {
    //  HistoricalRate
    fun saveHistoricalRate(rate: HistoricalRate)
    fun getHistoricalRate(coin: String, currency: String, timestamp: Long): HistoricalRate?

    //  ChartPoint
    fun getChartPoints(key: ChartInfoKey, fromTimestamp: Long): List<ChartPointEntity>
    fun saveChartPoints(points: List<ChartPointEntity>)
    fun deleteChartPoints(key: ChartInfoKey)

    //  MarketInfo
    fun getMarketInfo(coin: String, currency: String): MarketInfoEntity?
    fun getOldMarketInfo(coins: List<String>, currency: String): List<MarketInfoEntity>
    fun saveMarketInfo(marketInfoList: List<MarketInfoEntity>)
}

interface IMarketInfoProvider {
    fun getMarketInfo(coins: List<String>, currency: String): Single<List<MarketInfoEntity>>
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

interface ITopListProvider {
    fun getTopListCoins(currency: String): Single<List<CoinInfo>>
}
