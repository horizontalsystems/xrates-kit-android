package io.horizontalsystems.xrateskit.core

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.providers.InfoProvider
import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Single

interface IStorage {
    // Audit
    fun getAuditReports(coinType: CoinType): List<Auditor>
    fun saveAuditReports(coinType: CoinType, auditors: List<Auditor>)
    fun deleteCoinAuditReports(coinType: CoinType)

        //ResourceInfo
    fun getResourceInfo(resourceType: ResourceType): ResourceInfo?
    fun saveResourceInfo(resourceInfo: ResourceInfo)

        // coinInfo
    fun getCoinInfoCount(): Int
    fun getCoinInfos(): List<CoinInfoEntity>
    fun getExchangeInfo(exchangeId: String): ExchangeInfoEntity?
    fun getSecurityParameter(coinType: CoinType): SecurityParameter?
    fun getCoinCategories(coinType: CoinType): List<CoinCategory>
    fun getCoinFunds(coinType: CoinType): List<CoinFund>
    fun getCoinFundCategories(categoriesId: List<String>): List<CoinFundCategory>
    fun getCoinInfosByCategory(categoryId: String): List<CoinInfoEntity>
    fun getCoinInfo(coinType: CoinType): CoinInfoEntity?
    fun getCoinLinks(coinType: CoinType): List<CoinLinksEntity>
    fun getCategorizedCoinTypes(): List<CoinType>
    fun getCoinTreasuries(coinType: CoinType): List<CoinTreasuryEntity>
    fun getTreasuryCompanies(companyIds: List<String>): List<TreasuryCompany>
    fun saveCoinInfos(coinInfos: List<CoinInfoEntity>)
    fun saveSecurityParameters(params: List<SecurityParameter>)
    fun saveExchangeInfo(exchangeInfos: List<ExchangeInfoEntity>)
    fun saveCoinCategories(coinCategoryEntities: List<CoinCategoriesEntity>)
    fun saveCoinCategory(coinCategories: List<CoinCategory>)
    fun saveCoinLinks(coinLinks: List<CoinLinksEntity>)
    fun saveCoinFunds(coinFunds: List<CoinFundsEntity>)
    fun saveCoinFund(coinFunds: List<CoinFund>)
    fun saveCoinFundCategory(coinFundCategories: List<CoinFundCategory>)
    fun saveCoinTreasuries(coinTreasuries: List<CoinTreasuryEntity>)
    fun saveTreasuryCompanies(treasuryCompanies: List<TreasuryCompany>)
    fun deleteAllSecurityParameters()
    fun deleteAllCoinTreasuries()
    fun deleteAllTreasuryCompanies()
    fun deleteAllExchangeInfo()
    fun deleteAllCoinCategories()
    fun deleteAllCoinsCategories()
    fun deleteAllCoinLinks()
    fun deleteAllCoinFunds()
    fun deleteAllCoinsFunds()
    fun deleteAllCoinFundCategories()

        //Provider Coins
    fun saveProviderCoins(providerCoins: List<ProviderCoinEntity>)
    fun getProviderCoins(coinTypes: List<CoinType>): List<ProviderCoinEntity>
    fun getProviderCoin(coinType: CoinType): ProviderCoinEntity?
    fun getCoinTypesByProviderCoinId(providerCoinId: String, provider: InfoProvider): List<CoinType>
    fun searchCoins(searchText: String): List<ProviderCoinEntity>
    fun clearPriorities()
    fun setPriorityForCoin(coinType: CoinType, priority: Int)

    //  HistoricalRate
    fun saveHistoricalRate(rate: HistoricalRate)
    fun getHistoricalRate(coinType: CoinType, currencyCode: String, timestamp: Long): HistoricalRate?

    //  ChartPoint
    fun getChartPoints(key: ChartInfoKey): List<ChartPointEntity>
    fun saveChartPoints(points: List<ChartPointEntity>)
    fun deleteChartPoints(key: ChartInfoKey)

    //  LatestRate
    fun getLatestRate(coinType: CoinType, currencyCode: String): LatestRateEntity?
    fun getOldLatestRates(coinTypes: List<CoinType>, currencyCode: String): List<LatestRateEntity>
    fun saveLatestRates(marketInfoList: List<LatestRateEntity>)

    //  GlobalMarketInfo
    fun getGlobalMarketPointInfo(currencyCode: String, timePeriod: TimePeriod): GlobalCoinMarketPointInfo?
    fun deleteGlobalMarketPointInfo(currencyCode: String, timePeriod: TimePeriod)
    fun saveGlobalMarketPointInfo(globalCoinMarketPointInfo: GlobalCoinMarketPointInfo)

}

interface IInfoManager {
    fun destroy()
}

interface ICoinMarketManager {
    fun getTopCoinMarketsAsync(currency: String, fetchDiffPeriod: TimePeriod, itemsCount: Int): Single<List<CoinMarket>>
    fun getCoinMarketsAsync(coinTypes: List<CoinType>, currencyCode: String, fetchDiffPeriod: TimePeriod): Single<List<CoinMarket>>
    fun getCoinMarketDetailsAsync(coinType: CoinType, currencyCode: String, rateDiffCoinCodes: List<String>, coinDiffPeriods: List<TimePeriod>): Single<CoinMarketDetails>
    fun getCoinMarketPointsAsync(coinType: CoinType, currencyCode: String, fetchDiffPeriod: TimePeriod): Single<List<CoinMarketPoint>>
}

interface IInfoProvider {
    val provider: InfoProvider
    fun initProvider()
    fun destroy()
}

interface ILatestRateProvider {
    fun getLatestRatesAsync(coinTypes: List<CoinType>, currencyCode: String): Single<List<LatestRateEntity>>
}

interface IChartInfoProvider {
    fun getChartPointsAsync(chartPointKey: ChartInfoKey): Single<List<ChartPointEntity>>
}

interface IHistoricalRateProvider {
    fun getHistoricalRateAsync(coinType: CoinType, currencyCode: String, timestamp: Long): Single<HistoricalRate>
}

interface ICryptoNewsProvider {
    fun getNewsAsync(latestTimestamp: Long?): Single<List<CryptoNews>>
}

interface ICoinMarketProvider : IInfoProvider {
    fun getTopCoinMarketsAsync(currencyCode: String, fetchDiffPeriod: TimePeriod, itemsCount: Int, defiFilter: Boolean = false): Single<List<CoinMarket>>
    fun getCoinMarketsAsync(coinTypes: List<CoinType>, currencyCode: String, fetchDiffPeriod: TimePeriod): Single<List<CoinMarket>>
    fun getCoinMarketDetailsAsync(coinType: CoinType, currencyCode: String, rateDiffCoinIds: List<String>, rateDiffPeriods: List<TimePeriod>): Single<CoinMarketDetails>
    fun getCoinMarketPointsAsync(coinType: CoinType, currencyCode: String, fetchDiffPeriod: TimePeriod): Single<List<CoinMarketPoint>>
}

interface IGlobalCoinMarketProvider : IInfoProvider {
    fun getGlobalCoinMarketPointsAsync(currencyCode: String, timePeriod: TimePeriod): Single<List<GlobalCoinMarketPoint>>
}

interface ITokenInfoProvider : IInfoProvider {
    fun getTopTokenHoldersAsync(coinType: CoinType, itemsCount: Int): Single<List<TokenHolder>>
}

interface IAuditInfoProvider : IInfoProvider {
    fun getAuditReportsAsync(coinType: CoinType): Single<List<Auditor>>
}

interface IDefiMarketsProvider : IInfoProvider {
    fun getTopDefiTvlAsync(currencyCode: String, fetchDiffPeriod: TimePeriod, itemsCount: Int, chain: String?): Single<List<DefiTvl>>
    fun getDefiTvlAsync(coinType: CoinType, currencyCode: String): Single<DefiTvl>
    fun getDefiTvlPointsAsync(coinType: CoinType, currencyCode: String, timePeriod: TimePeriod): Single<List<DefiTvlPoint>>
}
