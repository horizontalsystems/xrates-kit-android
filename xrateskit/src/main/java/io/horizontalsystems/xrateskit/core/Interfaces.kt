package io.horizontalsystems.xrateskit.core

import io.horizontalsystems.xrateskit.api.InfoProvider
import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Single

interface IStorage {

    //  Provider Coin Info
    fun saveProviderCoinInfo(providerCoinInfos: List<ProviderCoinInfo>)
    fun getProviderCoinInfoByCodes(providerId:Int, coinCodes: List<String>): List<ProviderCoinInfo>
    fun getProviderCoinsInfoCount(providerId:Int): Int

    //  Coins
    fun saveCoinInfo(coinInfos: List<CoinInfoEntity>)
    fun getCoinInfoByCodes(coinCodes: List<String>): List<CoinInfoEntity>
    fun getCoinInfoCount(): Int

    //  HistoricalRate
    fun saveHistoricalRate(rate: HistoricalRate)
    fun getHistoricalRate(coinCode: String, currencyCode: String, timestamp: Long): HistoricalRate?

    //  ChartPoint
    fun getChartPoints(key: ChartInfoKey): List<ChartPointEntity>
    fun saveChartPoints(points: List<ChartPointEntity>)
    fun deleteChartPoints(key: ChartInfoKey)

    //  MarketInfo
    fun getMarketInfo(coinCode: String, currencyCode: String): MarketInfoEntity?
    fun getOldMarketInfo(coinCodes: List<String>, currencyCode: String): List<MarketInfoEntity>
    fun saveMarketInfo(marketInfoList: List<MarketInfoEntity>)

    //  GlobalMarketInfo
    fun getGlobalMarketInfo(currency: String): GlobalCoinMarket?
    fun saveGlobalMarketInfo(globalCoinMarket: GlobalCoinMarket)

}

interface IInfoManager {
    fun destroy()
}

interface IInfoProvider {
    val provider: InfoProvider
    fun initProvider()
    fun destroy()
}

interface ICoinInfoProvider {
    fun getCoinInfoAsync(platform: CoinType):Single<List<Coin>>
}

interface IFiatXRatesProvider {
    fun getLatestFiatXRates(sourceCurrency: String, targetCurrency: String): Double
}

interface IMarketInfoProvider {
    fun getMarketInfo(coins: List<Coin>, currency: String): Single<List<MarketInfoEntity>>
}

interface IChartInfoProvider {
    fun getChartPoints(chartPointKey: ChartInfoKey): Single<List<ChartPointEntity>>
}

interface IHistoricalRateProvider {
    fun getHistoricalRate(coin: String, currency: String, timestamp: Long): Single<HistoricalRate>
}

interface ICryptoNewsProvider {
    fun getNews(categories: String): Single<List<CryptoNews>>
}

interface ICoinMarketProvider : IInfoProvider {
    fun getTopCoinMarketsAsync(currencyCode: String, fetchDiffPeriod: TimePeriod, itemsCount: Int): Single<List<CoinMarket>>
    fun getCoinMarketsAsync(coins: List<Coin>, currencyCode: String, fetchDiffPeriod: TimePeriod): Single<List<CoinMarket>>
}

interface IGlobalCoinMarketProvider : IInfoProvider {
    fun getGlobalCoinMarketsAsync(currency: String): Single<GlobalCoinMarket>
}