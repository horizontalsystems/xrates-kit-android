package io.horizontalsystems.xrateskit.storage

import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.*

class Storage(private val database: Database) : IStorage {

    private val providerCoinInfoDao = database.providerCoinInfoDao
    private val historicalRateDao = database.historicalRateDao
    private val chartPointDao = database.chartPointDao
    private val marketInfoDao = database.marketInfoDao
    private val globalMarketInfoDao = database.globalMarketInfoDao
    private val coinInfoDao = database.coinInfoDao

    // Coin Info
    override fun getCoinInfoCount(): Int {
        return coinInfoDao.getCoinInfoCount()
    }

    override fun getCoinCategories(coinId: String): List<CoinCategory> {
        return coinInfoDao.getCoinCategories(coinId)
    }

    override fun getCoinInfo(categoryId: String): List<CoinInfoEntity> {
        return coinInfoDao.getCoinInfoByCategory(categoryId)
    }

    override fun saveCoinInfo(coinInfos: List<CoinInfoEntity>) {
        coinInfoDao.insertCoinInfo(coinInfos)
    }

    override fun saveCoinCategories(coinCategoryEntities: List<CoinCategoriesEntity>) {
        coinInfoDao.insertCoinCategories(coinCategoryEntities)
    }

    override fun saveCoinCategory(coinCategories: List<CoinCategory>){
        coinInfoDao.insertCoinCategory(coinCategories)
    }

    // Provider Coin Info

    override fun saveProviderCoinInfo(providerCoinInfos: List<ProviderCoinInfo>) {
        providerCoinInfoDao.insertAll(providerCoinInfos)
    }

    override fun getProviderCoinInfoByCodes(providerId:Int, coinCodes: List<String>): List<ProviderCoinInfo> {
        return providerCoinInfoDao.getCoinsByCodes(providerId, coinCodes)
    }

    override fun getProviderCoinsInfoCount(providerId:Int): Int {
        return providerCoinInfoDao.getRecordCount(providerId)
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
