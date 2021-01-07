package io.horizontalsystems.xrateskit.coinmarkets

import io.horizontalsystems.xrateskit.core.IGlobalCoinMarketProvider
import io.horizontalsystems.xrateskit.entities.GlobalCoinMarket
import io.horizontalsystems.xrateskit.storage.Storage
import io.reactivex.Single

class GlobalMarketInfoManager(
        private val globalMarketInfoProvider: IGlobalCoinMarketProvider,
        private val storage: Storage
) {
    fun getGlobalMarketInfo(currencyCode: String): Single<GlobalCoinMarket> {
        return globalMarketInfoProvider
            .getGlobalCoinMarketsAsync(currencyCode)
                .map { globalMarketInfo ->
                    storage.saveGlobalMarketInfo(globalMarketInfo)
                    globalMarketInfo
                }
                .onErrorReturn {
                    storage.getGlobalMarketInfo(currencyCode)
                }
    }

}
