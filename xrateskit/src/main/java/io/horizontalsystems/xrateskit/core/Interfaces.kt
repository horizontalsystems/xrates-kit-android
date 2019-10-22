package io.horizontalsystems.xrateskit.core

import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Single

interface IStorage {
    //  LatestRate
    fun saveLatestRates(rates: List<LatestRate>)
    fun getLatestRate(coin: String, currency: String): LatestRate?
    fun getOldLatestRates(coins: List<String>, currency: String): List<LatestRate>

    //  HistoricalRate
    fun saveHistoricalRate(rate: HistoricalRate)
    fun getHistoricalRate(coin: String, currency: String, timestamp: Long): HistoricalRate?

    //  ChartStats
    fun getChartPoints(key: ChartPointKey): List<ChartPoint>
    fun getLatestChartPoints(key: ChartPointKey): ChartPoint?
    fun saveChartPoints(points: List<ChartPoint>)
    fun deleteChartPoints(key: ChartPointKey)

    //  MarketStats
    fun getMarketStats(coin: String, currency: String): MarketStats?
    fun saveMarketStats(marketStats: MarketStats)
}

interface ILatestRateProvider {
    fun getLatestRates(coins: List<String>, currency: String): Single<Map<String, String>>
}

interface IChartPointProvider {
    fun getChartPoints(chartPointKey: ChartPointKey): Single<List<ChartPoint>>
}

interface IMarketStatsProvider {
    fun getMarketStats(coin: String, currency: String): Single<MarketStats>
}

interface IHistoricalRateProvider {
    fun getHistoricalRate(coin: String, currency: String, timestamp: Long): Single<HistoricalRate>
}
