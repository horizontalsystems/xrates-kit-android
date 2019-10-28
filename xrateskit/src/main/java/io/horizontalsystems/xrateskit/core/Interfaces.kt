package io.horizontalsystems.xrateskit.core

import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Single

interface IStorage {
    //  HistoricalRate
    fun saveHistoricalRate(rate: HistoricalRate)
    fun getHistoricalRate(coin: String, currency: String, timestamp: Long): HistoricalRate?

    //  ChartStats
    fun getChartPoints(key: ChartPointKey): List<ChartPointEntity>
    fun getChartPoints(key: ChartPointKey, fromTimestamp: Long): List<ChartPointEntity>
    fun getLatestChartPoints(key: ChartPointKey): ChartPointEntity?
    fun saveChartPoints(points: List<ChartPointEntity>)
    fun deleteChartPoints(key: ChartPointKey)

    //  MarketInfo
    fun getMarketInfo(coin: String, currency: String): MarketInfoEntity?
    fun getOldMarketInfo(coins: List<String>, currency: String): List<MarketInfoEntity>
    fun saveMarketInfo(marketInfoList: List<MarketInfoEntity>)
}

interface IMarketInfoProvider {
    fun getMarketInfo(coins: List<String>, currency: String): Single<List<MarketInfoEntity>>
}

interface IChartPointProvider {
    fun getChartPoints(chartPointKey: ChartPointKey): Single<List<ChartPointEntity>>
}

interface IHistoricalRateProvider {
    fun getHistoricalRate(coin: String, currency: String, timestamp: Long): Single<HistoricalRate>
}
