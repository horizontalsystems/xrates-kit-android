package io.horizontalsystems.xrateskit.demo

import android.content.Context
import io.horizontalsystems.xrateskit.XRatesKit
import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Observable
import io.reactivex.Single

class RatesManager(context: Context, currency: String) {
    private val kit = XRatesKit.create(context, currency, 60 * 10, uniswapGraphUrl = "https://api.thegraph.com/subgraphs/name/uniswap/uniswap-v2")

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

    fun topList(itemsCount:Int, currency: String, period: TimePeriod): Single<List<TopMarket>>{
        return kit.getTopMarketsAsync(itemsCount, currencyCode = currency, period)
    }

    fun topDefiList(itemsCount:Int, currency: String, period: TimePeriod): Single<List<TopMarket>>{
        return kit.getTopDefiMarketsAsync(itemsCount, currencyCode = currency, period)
    }

    fun globalMarketInfo(currency: String): Single<GlobalMarketInfo>{
        return kit.getGlobalMarketInfoAsync(currency)
    }

}
