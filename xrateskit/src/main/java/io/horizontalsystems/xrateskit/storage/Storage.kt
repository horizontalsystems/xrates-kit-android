package io.horizontalsystems.xrateskit.storage

import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.*

class Storage(database: Database) : IStorage {
    private val latestRateDao = database.latestRateDao
    private val historicalRateDao = database.historicalRateDao
    private val chartStatsDao = database.chartStatsDao
    private val marketStatsDao = database.marketStatsDao

    // LatestRate

    override fun saveLatestRate(rate: LatestRate) {
        latestRateDao.insert(rate)
    }

    override fun getLatestRate(coin: String, currency: String): LatestRate? {
        return latestRateDao.getRate(coin, currency)
    }

    // HistoricalRate

    override fun saveHistoricalRate(rate: HistoricalRate) {
        historicalRateDao.insert(rate)
    }

    override fun getHistoricalRate(coin: String, currency: String, timestamp: Long): HistoricalRate? {
        return historicalRateDao.getRate(coin, currency, timestamp)
    }

    //  ChartStats

    override fun getChartStats(coin: String, currency: String, type: ChartType): List<ChartStats> {
        return chartStatsDao.getList(coin, currency, type)
    }

    override fun saveChartStats(chartStats: List<ChartStats>) {
        chartStatsDao.insert(chartStats)
    }

    override fun getLatestChartStats(coin: String, currency: String, type: ChartType): ChartStats? {
        return chartStatsDao.getLast(coin, currency, type)
    }

    //  MarketStats

    override fun getMarketStats(coin: String, currency: String): MarketStats? {
        return marketStatsDao.getMarketStats(coin, currency)
    }

    override fun saveMarketStats(marketStats: MarketStats) {
        marketStatsDao.insert(marketStats)
    }
}
