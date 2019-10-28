package io.horizontalsystems.xrateskit.storage

import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.ChartPointEntity
import io.horizontalsystems.xrateskit.entities.ChartPointKey
import io.horizontalsystems.xrateskit.entities.HistoricalRate
import io.horizontalsystems.xrateskit.entities.MarketInfoEntity

class Storage(database: Database) : IStorage {
    private val historicalRateDao = database.historicalRateDao
    private val chartPointDao = database.chartPointDao
    private val marketInfoDao = database.marketInfoDao

    // HistoricalRate

    override fun saveHistoricalRate(rate: HistoricalRate) {
        historicalRateDao.insert(rate)
    }

    override fun getHistoricalRate(coin: String, currency: String, timestamp: Long): HistoricalRate? {
        return historicalRateDao.getRate(coin, currency, timestamp)
    }

    //  ChartPoint

    override fun getChartPoints(key: ChartPointKey): List<ChartPointEntity> {
        return chartPointDao.getList(key.coin, key.currency, key.chartType)
    }

    override fun getChartPoints(key: ChartPointKey, fromTimestamp: Long): List<ChartPointEntity> {
        return chartPointDao.getList(key.coin, key.currency, key.chartType, fromTimestamp)
    }

    override fun getLatestChartPoints(key: ChartPointKey): ChartPointEntity? {
        return chartPointDao.getLast(key.coin, key.currency, key.chartType)
    }

    override fun saveChartPoints(points: List<ChartPointEntity>) {
        chartPointDao.insert(points)
    }

    override fun deleteChartPoints(key: ChartPointKey) {
        chartPointDao.delete(key.coin, key.currency, key.chartType)
    }

    //  MarketStats

    override fun getMarketInfo(coin: String, currency: String): MarketInfoEntity? {
        return marketInfoDao.getMarketInfo(coin, currency)
    }

    override fun getOldMarketInfo(coins: List<String>, currency: String): List<MarketInfoEntity> {
        return marketInfoDao.getOldList(coins, currency)
    }

    override fun saveMarketInfo(marketInfoList: List<MarketInfoEntity>) {
        marketInfoDao.insertAll(marketInfoList)
    }
}
