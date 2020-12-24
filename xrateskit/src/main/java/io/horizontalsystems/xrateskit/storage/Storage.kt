package io.horizontalsystems.xrateskit.storage

import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.*

class Storage(
        private val database: Database
) : IStorage {

    private val historicalRateDao = database.historicalRateDao
    private val chartPointDao = database.chartPointDao
    private val marketInfoDao = database.marketInfoDao
    private val topMarketCoinDao = database.topMarketCoinDao
    private val globalMarketInfoDao = database.globalMarketInfoDao

    // HistoricalRate

    override fun saveHistoricalRate(rate: HistoricalRate) {
        historicalRateDao.insert(rate)
    }

    override fun getHistoricalRate(coin: String, currency: String, timestamp: Long): HistoricalRate? {
        return historicalRateDao.getRate(coin, currency, timestamp)
    }

    //  ChartPoint

    override fun getChartPoints(key: ChartInfoKey): List<ChartPointEntity> {
        return chartPointDao.getList(key.coin, key.currency, key.chartType)
    }

    override fun saveChartPoints(points: List<ChartPointEntity>) {
        chartPointDao.insert(points)
    }

    override fun deleteChartPoints(key: ChartInfoKey) {
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

    // GlobalMarketInfo
    override fun saveGlobalMarketInfo(globalMarketInfo: GlobalMarketInfo) {
        globalMarketInfoDao.insert(globalMarketInfo)
    }

    override fun getGlobalMarketInfo(currencyCode: String): GlobalMarketInfo? {
        return globalMarketInfoDao.getGlobalMarketInfo(currencyCode)
    }
    // Top markets

    override fun getTopMarketCoins(): List<TopMarketCoin> {
        return topMarketCoinDao.getAll()
    }

    private fun saveTopMarketCoins(list: List<TopMarketCoin>) {
        topMarketCoinDao.deleteAll()
        topMarketCoinDao.insertAll(list)
    }

    override fun saveTopMarkets(topMarkets: List<TopMarket>) {
        database.runInTransaction {
            saveTopMarketCoins(topMarkets.map { TopMarketCoin(it.coin.code, it.coin.title) })

            marketInfoDao.insertAll(topMarkets.map { topMarket ->
                val entity: MarketInfoEntity
                topMarket.marketInfo.apply {
                    entity = MarketInfoEntity(topMarket.coin.code,
                                              currencyCode,
                                              rate,
                                              rateOpenDay,
                                              rateDiff,
                                              volume,
                                              marketCap,
                                              supply,
                                              timestamp,
                                              liquidity,
                                              rateDiff1h,
                                              rateDiff24h,
                                              rateDiff7d,
                                              rateDiff30d,
                                              rateDiff1y)
                }
                entity
            })
        }
    }

}
