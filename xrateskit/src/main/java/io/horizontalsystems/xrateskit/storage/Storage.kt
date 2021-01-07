package io.horizontalsystems.xrateskit.storage

import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.*

class Storage(
        private val database: Database
) : IStorage {

    private val coinDao = database.coinDao
    private val historicalRateDao = database.historicalRateDao
    private val chartPointDao = database.chartPointDao
    private val marketInfoDao = database.marketInfoDao
    private val globalMarketInfoDao = database.globalMarketInfoDao

    // Coins

    override fun saveCoins(coins: List<CoinEntity>) {
        coinDao.insertAll(coins)
    }

    override fun getCoinsByCodes(coinCodes: List<String>): List<CoinEntity> {
        return coinDao.getCoinsByCodes(coinCodes)
    }

    // HistoricalRate

    override fun saveHistoricalRate(rate: HistoricalRate) {
        historicalRateDao.insert(rate)
    }

    override fun getHistoricalRate(coinCode: String, currencyCode: String, timestamp: Long): HistoricalRate? {
        return historicalRateDao.getRate(coinCode, currencyCode, timestamp)
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

    override fun getMarketInfo(coinCode: String, currencyCode: String): MarketInfoEntity? {
        return marketInfoDao.getMarketInfo(coinCode, currencyCode)
    }

    override fun getOldMarketInfo(coinCodes: List<String>, currencyCode: String): List<MarketInfoEntity> {
        return marketInfoDao.getOldList(coinCodes, currencyCode)
    }

    override fun saveMarketInfo(marketInfoList: List<MarketInfoEntity>) {
        marketInfoDao.insertAll(marketInfoList)
    }

    // GlobalMarketInfo
    override fun saveGlobalMarketInfo(globalCoinMarket: GlobalCoinMarket) {
        globalMarketInfoDao.insert(globalCoinMarket)
    }

    override fun getGlobalMarketInfo(currencyCode: String): GlobalCoinMarket? {
        return globalMarketInfoDao.getGlobalMarketInfo(currencyCode)
    }

}
