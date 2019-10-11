package io.horizontalsystems.xrateskit.core

import io.horizontalsystems.xrateskit.entities.ChartStats
import io.horizontalsystems.xrateskit.entities.ChartType
import io.horizontalsystems.xrateskit.entities.MarketStats
import io.horizontalsystems.xrateskit.entities.HistoricalRate
import io.horizontalsystems.xrateskit.entities.LatestRate
import io.reactivex.Observable
import io.reactivex.Single

interface IStorage {
    //  LatestRate
    fun saveLatestRate(rate: LatestRate)
    fun getLatestRate(coin: String, currency: String): LatestRate?

    //  HistoricalRate
    fun saveHistoricalRate(rate: HistoricalRate)
    fun getHistoricalRate(coin: String, currency: String, timestamp: Long): HistoricalRate?

    //  ChartStats
    fun getChartStats(coin: String, currency: String, type: ChartType): List<ChartStats>
    fun saveChartStats(chartStats: List<ChartStats>)
    fun getLatestChartStats(coin: String, currency: String, type: ChartType): ChartStats?

    //  MarketStats
    fun getMarketStats(coin: String, currency: String): MarketStats?
    fun saveMarketStats(marketStats: MarketStats)
}

interface ILatestRateProvider {
    fun getLatestRate(coins: List<String>, currency: String): Observable<LatestRate>
}

interface IChartStatsProvider {
    fun getChartStats(coin: String, currency: String, type: ChartType): Single<List<ChartStats>>
}

interface IMarketStatsProvider {
    fun getMarketStats(coin: String, currency: String): Single<MarketStats>
}

interface IHistoricalRateProvider {
    fun getHistoricalRate(coin: String, currency: String, timestamp: Long): Single<HistoricalRate>
}

interface ISyncCompletionListener {
    fun onSuccess()
    fun onFail()
}
