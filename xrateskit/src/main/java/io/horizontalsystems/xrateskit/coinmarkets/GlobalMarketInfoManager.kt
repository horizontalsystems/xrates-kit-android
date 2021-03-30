package io.horizontalsystems.xrateskit.coinmarkets

import io.horizontalsystems.xrateskit.core.IFiatXRatesProvider
import io.horizontalsystems.xrateskit.core.IGlobalCoinMarketProvider
import io.horizontalsystems.xrateskit.core.IInfoManager
import io.horizontalsystems.xrateskit.entities.GlobalCoinMarket
import io.horizontalsystems.xrateskit.storage.Storage
import io.reactivex.Single
import java.math.BigDecimal

class GlobalMarketInfoManager(
    private val globalMarketsProvider: IGlobalCoinMarketProvider,
    private val defiMarketsProvider: IGlobalCoinMarketProvider,
    private val fiatXRatesProvider: IFiatXRatesProvider,
    private val storage: Storage
) : IInfoManager {

    private val BASE_FIAT_CURRENCY = "USD"

    fun getGlobalMarketInfo(currencyCode: String): Single<GlobalCoinMarket> {
        return Single.zip(
            globalMarketsProvider.getGlobalCoinMarketsAsync(currencyCode),
            defiMarketsProvider.getGlobalCoinMarketsAsync(currencyCode),
            getFiatXRates(currencyCode),
            { globalMarket, defiMarket , fiatXRate ->
                globalMarket.volume24h = globalMarket.volume24h * fiatXRate.toBigDecimal()
                globalMarket.marketCap = globalMarket.marketCap * fiatXRate.toBigDecimal()
                globalMarket.defiMarketCap = defiMarket.defiMarketCap * fiatXRate.toBigDecimal()
                globalMarket.defiMarketCapDiff24h = defiMarket.defiMarketCapDiff24h
                globalMarket.defiTvl = defiMarket.defiTvl * fiatXRate.toBigDecimal()
                globalMarket.defiTvlDiff24h = defiMarket.defiTvlDiff24h
                globalMarket
            }).map { globalMarketInfo ->
            storage.saveGlobalMarketInfo(globalMarketInfo)
                globalMarketInfo
            }.onErrorReturn {
                storage.getGlobalMarketInfo(currencyCode)
            }
    }

    private fun getFiatXRates(currencyCode: String): Single<Double>{
        if(currencyCode.toUpperCase().contentEquals(BASE_FIAT_CURRENCY)){
            return Single.just(1.0)
        }

        return fiatXRatesProvider.getLatestFiatXRates(BASE_FIAT_CURRENCY, currencyCode)
    }

    override fun destroy() {
        globalMarketsProvider.destroy()
    }
}
