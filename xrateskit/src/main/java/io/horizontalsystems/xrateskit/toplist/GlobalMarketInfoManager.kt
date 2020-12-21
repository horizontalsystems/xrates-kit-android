package io.horizontalsystems.xrateskit.toplist

import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.IGlobalMarketInfoProvider
import io.horizontalsystems.xrateskit.core.ITopMarketsProvider
import io.horizontalsystems.xrateskit.entities.GlobalMarketInfo
import io.horizontalsystems.xrateskit.entities.TopMarket
import io.horizontalsystems.xrateskit.storage.Storage
import io.reactivex.Single

class GlobalMarketInfoManager(
        private val globalMarketInfoProvider: IGlobalMarketInfoProvider,
        private val factory: Factory,
        private val storage: Storage
) {
    fun getGlobalMarketInfo(): Single<GlobalMarketInfo> {
        return globalMarketInfoProvider
            .getGlobalMarketInfo()
                .map { globalMarketInfo ->
                    //storage.saveTopMarkets(topMarkets)
                    globalMarketInfo
                }
                .onErrorReturn {
                    GlobalMarketInfo(0.0,0.0,0.0,0.0,0.0,0.0)
//                    val topMarketCoins = storage.getTopMarketCoins()
//                    val oldMarketInfos = storage.getOldMarketInfo(topMarketCoins.map { it.code }, currency)
//
//                    topMarketCoins.mapNotNull { coin ->
//                        oldMarketInfos.firstOrNull { it.coin == coin.code }?.let { marketInfo ->
//                            factory.createTopMarket(coin, marketInfo)
//                        }
//                    }
                }
    }

}
