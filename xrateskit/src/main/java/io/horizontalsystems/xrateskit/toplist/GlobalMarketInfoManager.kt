package io.horizontalsystems.xrateskit.toplist

import io.horizontalsystems.xrateskit.core.IGlobalMarketInfoProvider
import io.horizontalsystems.xrateskit.entities.GlobalMarketInfo
import io.horizontalsystems.xrateskit.storage.Storage
import io.reactivex.Single

class GlobalMarketInfoManager(
        private val globalMarketInfoProvider: IGlobalMarketInfoProvider,
        private val storage: Storage
) {
    fun getGlobalMarketInfo(currencyCode: String): Single<GlobalMarketInfo> {
        return globalMarketInfoProvider
            .getGlobalMarketInfoAsync(currencyCode)
                .map { globalMarketInfo ->
                    storage.saveGlobalMarketInfo(globalMarketInfo)
                    globalMarketInfo
                }
                .onErrorReturn {
                    storage.getGlobalMarketInfo(currencyCode)
                }
    }

}
