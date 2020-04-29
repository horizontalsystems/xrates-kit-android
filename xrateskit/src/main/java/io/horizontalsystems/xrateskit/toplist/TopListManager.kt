package io.horizontalsystems.xrateskit.toplist

import io.horizontalsystems.xrateskit.api.CryptoCompareProvider
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.entities.TopMarket
import io.horizontalsystems.xrateskit.marketinfo.MarketInfoManager
import io.reactivex.Single

class TopListManager(
    private val provider: CryptoCompareProvider,
    private val factory: Factory,
    private val marketInfoManager: MarketInfoManager
) {

    fun getTopList(currency: String): Single<List<TopMarket>> {
        return provider.getTopListCoins(currency)
            .flatMap { coinInfos ->
                val coinCodes = coinInfos.map { it.coinCode }
                provider.getMarketInfo(coinCodes, currency)
                    .doOnSuccess {
                        marketInfoManager.update(it, currency)
                    }
                    .map { list ->
                        list.map { marketInfoEntity ->
                            factory.createTopMarket(coinInfos.first { marketInfoEntity.coin == it.coinCode }, marketInfoEntity)
                        }
                    }
            }
    }

}
