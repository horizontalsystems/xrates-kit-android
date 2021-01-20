package io.horizontalsystems.xrateskit.coinmarkets

import io.horizontalsystems.xrateskit.core.IGlobalCoinMarketProvider
import io.horizontalsystems.xrateskit.core.IInfoManager
import io.horizontalsystems.xrateskit.entities.GlobalCoinMarket
import io.horizontalsystems.xrateskit.storage.Storage
import io.reactivex.Single

class GlobalMarketInfoManager(
    private val globalMarketsProvider: IGlobalCoinMarketProvider,
    private val defiMarketsProvider: IGlobalCoinMarketProvider,
    private val storage: Storage
) : IInfoManager {
    fun getGlobalMarketInfo(currencyCode: String): Single<GlobalCoinMarket> {
        return Single.zip(
            globalMarketsProvider.getGlobalCoinMarketsAsync(currencyCode),
            defiMarketsProvider.getGlobalCoinMarketsAsync(currencyCode),
            { globalMarket, defiMarket ->
                globalMarket.defiMarketCap = defiMarket.defiMarketCap
                globalMarket.defiMarketCapDiff24h = defiMarket.defiMarketCapDiff24h
                globalMarket
            }).map { globalMarketInfo ->
            storage.saveGlobalMarketInfo(globalMarketInfo)
                globalMarketInfo
            }.onErrorReturn {
                storage.getGlobalMarketInfo(currencyCode)
            }
    }

    override fun destroy() {
        globalMarketsProvider.destroy()
    }
}
