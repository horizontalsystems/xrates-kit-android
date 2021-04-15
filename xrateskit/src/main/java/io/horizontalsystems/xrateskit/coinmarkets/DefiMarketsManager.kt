package io.horizontalsystems.xrateskit.coinmarkets

import io.horizontalsystems.xrateskit.core.IDefiMarketsProvider
import io.horizontalsystems.xrateskit.core.IInfoManager
import io.horizontalsystems.xrateskit.entities.DefiMarket
import io.horizontalsystems.xrateskit.storage.Storage
import io.reactivex.Single

class DefiMarketsManager(
    private val defiMarketsProvider: IDefiMarketsProvider
) : IInfoManager {
    fun getTopDefiMarketsAsync(currencyCode: String, itemsCount: Int): Single<List<DefiMarket>> {
        return defiMarketsProvider.getGlobalCoinMarketPointsAsync(currencyCode, itemsCount)
    }

    override fun destroy() {
        defiMarketsProvider.destroy()
    }
}