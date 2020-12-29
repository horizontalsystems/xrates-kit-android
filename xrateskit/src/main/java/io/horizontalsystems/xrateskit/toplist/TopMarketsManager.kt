package io.horizontalsystems.xrateskit.toplist

import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.ITopDefiMarketsProvider
import io.horizontalsystems.xrateskit.core.ITopMarketsProvider
import io.horizontalsystems.xrateskit.entities.Coin
import io.horizontalsystems.xrateskit.entities.TimePeriod
import io.horizontalsystems.xrateskit.entities.TopMarket
import io.horizontalsystems.xrateskit.storage.Storage
import io.reactivex.Single

class TopMarketsManager(
    private val topMarketsProvider: ITopMarketsProvider,
    private val topDefiMarketsProvider: ITopDefiMarketsProvider,
    private val factory: Factory,
    private val storage: Storage
) {
    fun getTopMarkets( currency: String, fetchDiffPeriod: TimePeriod = TimePeriod.HOUR_24, itemsCout: Int): Single<List<TopMarket>> {
        return topMarketsProvider
                .getTopMarketsAsync( currency, fetchDiffPeriod, itemsCout)
                .map { topMarkets ->
                    //storage.saveTopMarkets(topMarkets)
                    topMarkets
                }
                .onErrorReturn {
                    val topMarketCoins = storage.getTopMarketCoins()
                    val oldMarketInfos = storage.getOldMarketInfo(topMarketCoins.map { it.code }, currency)

                    topMarketCoins.mapNotNull { topMarketCoin ->
                        oldMarketInfos.firstOrNull { it.coinCode == topMarketCoin.code }?.let { marketInfo ->
                            factory.createTopMarket(Coin(topMarketCoin.code, topMarketCoin.name), marketInfo)
                        }
                    }
                }
    }

    fun getTopDefiMarkets(currency: String, fetchDiffPeriod: TimePeriod = TimePeriod.HOUR_24, itemsCout: Int): Single<List<TopMarket>> {
        return topDefiMarketsProvider.getTopMarketsAsync(currency, fetchDiffPeriod, itemsCout)
    }

}
