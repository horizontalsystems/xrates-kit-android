package io.horizontalsystems.xrateskit.demo

import android.content.Context
import io.horizontalsystems.xrateskit.XRatesKit
import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Observable
import io.reactivex.Single

class RatesManager(context: Context, currency: String) {
    private val kit = XRatesKit.create(context, currency, 60 * 10)

    fun set(coins: List<Coin>) {
        kit.set(coins)
    }

    fun set(currency: String) {
        kit.set(currency)
    }

    fun refresh() {
        kit.refresh()
    }

    fun marketInfoObservable(coinCode: String, currencyCode: String): Observable<MarketInfo> {
        return kit.marketInfoObservable(coinCode, currencyCode)
    }

    fun marketInfoObservable(currencyCode: String): Observable<Map<String, MarketInfo>> {
        return kit.marketInfoMapObservable(currencyCode)
    }

    fun marketInfo(coin: String, currency: String): MarketInfo? {
        return kit.getMarketInfo(coin, currency)
    }

    fun chartInfo(coin: String, currency: String, chartType: ChartType): ChartInfo? {
        return kit.getChartInfo(coin, currency, chartType)
    }

    fun chartInfoObservable(coin: String, currency: String, chartType: ChartType): Observable<ChartInfo> {
        return kit.chartInfoObservable(coin, currency, chartType)
    }

    fun topList(itemsCount:Int, currency: String, period: TimePeriod): Single<List<CoinMarket>>{
        return kit.getTopCoinMarketsAsync(currency, period, itemsCount)
    }

    fun favorites(coins: List<Coin>, currency: String, period: TimePeriod): Single<List<CoinMarket>>{
        return kit.getCoinMarketsAsync(coins, currency, period)
    }

    fun globalMarketInfo(currency: String): Single<GlobalCoinMarket>{
        return kit.getGlobalCoinMarketsAsync(currency)
    }

}
