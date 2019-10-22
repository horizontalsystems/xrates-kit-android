package io.horizontalsystems.xrateskit.storage

import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.*

class Storage(database: Database) : IStorage {
    private val latestRateDao = database.latestRateDao
    private val historicalRateDao = database.historicalRateDao
    private val chartStatsDao = database.chartStatsDao
    private val marketStatsDao = database.marketStatsDao

    // LatestRate

    override fun saveLatestRates(rates: List<LatestRate>) {
        latestRateDao.insert(rates)
    }

    override fun getLatestRate(coin: String, currency: String): LatestRate? {
        return latestRateDao.getRate(coin, currency)
    }

    override fun getOldLatestRates(coins: List<String>, currency: String): List<LatestRate> {
        return latestRateDao.getOldRates(coins, currency) // expired last
    }

    // HistoricalRate

    override fun saveHistoricalRate(rate: HistoricalRate) {
        historicalRateDao.insert(rate)
    }

    override fun getHistoricalRate(coin: String, currency: String, timestamp: Long): HistoricalRate? {
        return historicalRateDao.getRate(coin, currency, timestamp)
    }

    //  ChartPoint

    override fun getChartPoints(key: ChartPointKey): List<ChartPoint> {
        return chartStatsDao.getList(key.coin, key.currency, key.chartType)
    }

    override fun getLatestChartPoints(key: ChartPointKey): ChartPoint? {
        return chartStatsDao.getLast(key.coin, key.currency, key.chartType)
    }

    override fun saveChartPoints(points: List<ChartPoint>) {
        chartStatsDao.insert(points)
    }

    override fun deleteChartPoints(key: ChartPointKey) {
        chartStatsDao.delete(key.coin, key.currency, key.chartType)
    }

    //  MarketStats

    override fun getMarketStats(coin: String, currency: String): MarketStats? {
        return marketStatsDao.getMarketStats(coin, currency)
    }

    override fun saveMarketStats(marketStats: MarketStats) {
        marketStatsDao.insert(marketStats)
    }
}
