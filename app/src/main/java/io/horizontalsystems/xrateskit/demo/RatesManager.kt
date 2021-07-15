package io.horizontalsystems.xrateskit.demo

import android.content.Context
import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.XRatesKit
import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Observable
import io.reactivex.Single
import java.math.BigDecimal

class RatesManager(context: Context, currency: String) {
    private val kit = XRatesKit.create(context, 60 * 10, defiyieldProviderApiKey = "87e8671e-8267-427c-92c3-4627833445ae", coinsRemoteUrl = "https://raw.githubusercontent.com/horizontalsystems/cryptocurrencies/version/0.21/coins.json", providerCoinsRemoteUrl = "https://raw.githubusercontent.com/horizontalsystems/cryptocurrencies/version/0.21/provider.coins.json")

    fun refresh(currencyCode: String) {
        kit.refresh(currencyCode)
    }

    fun getLatestRateAsync(coinTypes: List<CoinType>, currencyCode: String): Observable<Map<CoinType, LatestRate>> {
        return kit.latestRateMapObservable(coinTypes, currencyCode)
    }

    fun getNewsAsync(): Single<List<CryptoNews>> {
        return kit.cryptoNewsAsync()
    }

    fun latestRate(coinType: CoinType, currency: String): LatestRate? {
        return kit.getLatestRate(coinType, currency)
    }

    fun chartInfo(coinType: CoinType, currency: String, chartType: ChartType): ChartInfo? {
        return kit.getChartInfo(coinType, currency, chartType)
    }

    fun chartInfoObservable(coinType: CoinType, currency: String, chartType: ChartType): Observable<ChartInfo> {
        return kit.chartInfoObservable(coinType, currency, chartType)
    }

    fun topList(itemsCount:Int, currency: String, period: TimePeriod): Single<List<CoinMarket>>{
        return kit.getTopCoinMarketsAsync(currency, period, itemsCount)
    }

    fun topDefiTvlList(itemsCount: Int, currency: String): Single<List<DefiTvl>>{
        return kit.getTopDefiTvlAsync(currency, TimePeriod.HOUR_24, itemsCount)
    }

    fun defiTvl(coinType: CoinType, currency: String): Single<DefiTvl>{
        return kit.getDefiTvlAsync(coinType, currency)
    }

    fun getCoinMarketDetailsAsync(coinType: CoinType, currencyCode: String, rateDiffCoinCodes: List<String>, rateDiffPeriods: List<TimePeriod>): Single<CoinMarketDetails> {
        return kit.getCoinMarketDetailsAsync(coinType, currencyCode, rateDiffCoinCodes, rateDiffPeriods)
    }

    fun favorites(coinTypes: List<CoinType>, currency: String, period: TimePeriod): Single<List<CoinMarket>>{
        return kit.getCoinMarketsAsync(coinTypes, currency, period)
    }

    fun getMarketsByCategory(categoryId: String, currency: String, period: TimePeriod): Single<List<CoinMarket>>{
        return kit.getCoinMarketsByCategoryAsync(categoryId, currency, period)
    }

    fun getHistoRates(coinType: CoinType, currency: String, timestamp: Long): Single<BigDecimal>{
        return kit.getHistoricalRateAsync(coinType, currency, timestamp)
    }

    fun getCoinMarketPoints(coinType: CoinType, currency: String, period: TimePeriod): Single<List<CoinMarketPoint>>{
        return kit.getCoinMarketPointsAsync(coinType, currency, period)
    }

    fun getAuditInfo(coinType: CoinType): Single<List<Auditor>>{
        return kit.getAuditReportsAsync(coinType)
    }

    fun globalMarketInfo(currency: String): Single<GlobalCoinMarket>{
        return kit.getGlobalCoinMarketsAsync(currency)
    }

    fun searchCoins(text: String): List<CoinData>{
        return kit.searchCoins(text)
    }

    fun getTopTokenHolders(coinType: CoinType, itemsCount: Int): Single<List<TokenHolder>> {
        return kit.getTopTokenHoldersAsync(coinType, itemsCount)
    }

}
