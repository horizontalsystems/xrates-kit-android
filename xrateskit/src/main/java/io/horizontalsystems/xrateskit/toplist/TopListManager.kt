package io.horizontalsystems.xrateskit.toplist

import io.horizontalsystems.xrateskit.api.CryptoCompareProvider
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.entities.TopMarket
import io.reactivex.Single

class TopListManager(private val provider: CryptoCompareProvider, private val factory: Factory) {

    fun getTopList(currency: String): Single<List<TopMarket>> {
        return provider.getTopListCoins(currency)
            .flatMap { coinInfos ->
                val coinCodes = coinInfos.map { it.coinCode }
                provider.getMarketInfo(coinCodes, currency)
                    .map { list ->
                        list.map { marketInfoEntity ->
                            factory.createTopMarket(coinInfos.first { marketInfoEntity.coin == it.coinCode }, marketInfoEntity)
                        }
                    }
            }
    }

}
