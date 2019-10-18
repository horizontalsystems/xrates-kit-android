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
    fun getChartStats(coin: String, currency: String, chartType: ChartType): List<ChartStats>
    fun saveChartStats(chartStats: List<ChartStats>)
    fun getLatestChartStats(coin: String, currency: String, chartType: ChartType): ChartStats?
    fun getOldChartStats(chartTypes: List<ChartType>, coins: List<String>, currency: String): List<ChartStats>

    //  MarketStats
    fun getMarketStats(coin: String, currency: String): MarketStats?
    fun saveMarketStats(marketStats: MarketStats)
}

interface ILatestRateProvider {
    fun getLatestRate(coins: List<String>, currency: String): Single<Map<String, String>>
}

interface IChartStatsProvider {
    fun getChartStats(coin: String, currency: String, chartType: ChartType): Single<List<ChartStats>>
}

interface IMarketStatsProvider {
    fun getMarketStats(coin: String, currency: String): Single<MarketStats>
}

interface IHistoricalRateProvider {
    fun getHistoricalRate(coin: String, currency: String, timestamp: Long): Single<HistoricalRate>
}
