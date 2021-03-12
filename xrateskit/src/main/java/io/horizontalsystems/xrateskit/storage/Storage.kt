package io.horizontalsystems.xrateskit.storage

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.providers.InfoProvider
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.*

class Storage(private val database: Database) : IStorage {

    private val providerCoinDao = database.providerCoinsDao
    private val historicalRateDao = database.historicalRateDao
    private val chartPointDao = database.chartPointDao
    private val latestRateDao = database.latestRatesDao
    private val globalMarketInfoDao = database.globalMarketInfoDao
    private val coinInfoDao = database.coinInfoDao
    private val resourceInfoDao = database.resourceInfoDao

    //Resource
    override fun getResourceInfo(resourceType: ResourceType): ResourceInfo? {
        return resourceInfoDao.getResourceInfo(resourceType.name)
    }

    override fun saveResourceInfo(resourceInfo: ResourceInfo) {
        resourceInfoDao.insertResouceInfo(resourceInfo)
    }

    // Coin Info
    override fun getCoinInfoCount(): Int {
        return coinInfoDao.getCoinInfoCount()
    }
    
    override fun getCoinInfo(coinType: CoinType): CoinInfoEntity? {
        return coinInfoDao.getCoinInfo(coinType)
    }

    override fun getCoinLinks(coinType: CoinType): List<CoinLinksEntity> {
        return coinInfoDao.getCoinLinks(coinType)
    }

    override fun getCoinInfos(): List<CoinInfoEntity> {
        return coinInfoDao.getCoinInfos()
    }

    override fun getCoinCategories(coinType: CoinType): List<CoinCategory> {
        return coinInfoDao.getCoinCategories(coinType)
    }

    override fun getCoinInfosByCategory(categoryId: String): List<CoinInfoEntity> {
        return coinInfoDao.getCoinInfoByCategory(categoryId)
    }

    override fun saveCoinInfos(coinInfos: List<CoinInfoEntity>) {
        coinInfoDao.insertCoinInfo(coinInfos)
    }

    override fun saveCoinCategories(coinCategoryEntities: List<CoinCategoriesEntity>) {
        coinInfoDao.insertCoinCategories(coinCategoryEntities)
    }

    override fun saveCoinCategory(coinCategories: List<CoinCategory>){
        coinInfoDao.insertCoinCategory(coinCategories)
    }

    override fun saveCoinLinks(coinLinks: List<CoinLinksEntity>){
        coinInfoDao.insertCoinLinks(coinLinks)
    }

    override fun deleteAllCoinCategories(){
        coinInfoDao.deleteAllCoinCategories()
    }

    override fun deleteAllCoinsCategories(){
        coinInfoDao.deleteAllCoinsCategories()
    }

    override fun deleteAllCoinLinks(){
        coinInfoDao.deleteAllCoinLinks()
    }


    // Provider Coin Info
    override fun searchCoins(searchText: String): List<ProviderCoinEntity> {
        return providerCoinDao.searchCoins("%${searchText}%")
    }

    override fun saveProviderCoins(providerCoins: List<ProviderCoinEntity>) {
        providerCoinDao.insertAll(providerCoins)
    }

    override fun getProviderCoins(coinTypes: List<CoinType>): List<ProviderCoinEntity> {
        return providerCoinDao.getProviderCoins(coinTypes)
    }

    override  fun getCoinTypesByProviderCoinId(providerCoinId: String, provider: InfoProvider): List<CoinType> {
        if(provider is InfoProvider.CoinGecko)
            return providerCoinDao.getCoinTypesForCoinGecko(providerCoinId)
        else
            return providerCoinDao.getCoinTypesForCryptoCompare(providerCoinId)
    }

    // HistoricalRate

    override fun saveHistoricalRate(rate: HistoricalRate) {
        historicalRateDao.insert(rate)
    }

    override fun getHistoricalRate(coinType: CoinType, currencyCode: String, timestamp: Long): HistoricalRate? {
        return historicalRateDao.getRate(coinType, currencyCode, timestamp)
    }

    //  ChartPoint

    override fun getChartPoints(key: ChartInfoKey): List<ChartPointEntity> {
        return chartPointDao.getList(key.coinType, key.currency, key.chartType)
    }

    override fun saveChartPoints(points: List<ChartPointEntity>) {
        chartPointDao.insert(points)
    }

    override fun deleteChartPoints(key: ChartInfoKey) {
        chartPointDao.delete(key.coinType, key.currency, key.chartType)
    }

    //  MarketStats

    override fun getLatestRate(coinType: CoinType, currencyCode: String): LatestRateEntity? {
        return latestRateDao.getLatestRate(coinType, currencyCode)
    }

    override fun getOldLatestRates(coinTypes: List<CoinType>, currencyCode: String): List<LatestRateEntity> {
        return latestRateDao.getOldList(coinTypes, currencyCode)
    }

    override fun saveLatestRates(marketInfoList: List<LatestRateEntity>) {
        latestRateDao.insertAll(marketInfoList)
    }

    // GlobalMarketInfo
    override fun saveGlobalMarketInfo(globalCoinMarket: GlobalCoinMarket) {
        globalMarketInfoDao.insert(globalCoinMarket)
    }

    override fun getGlobalMarketInfo(currencyCode: String): GlobalCoinMarket? {
        return globalMarketInfoDao.getGlobalMarketInfo(currencyCode)
    }

}
