package io.horizontalsystems.xrateskit.api

import io.horizontalsystems.xrateskit.api.graphproviders.UniswapGraphProvider
import io.horizontalsystems.xrateskit.core.IMarketInfoProvider
import io.horizontalsystems.xrateskit.entities.Coin
import io.horizontalsystems.xrateskit.entities.CoinType
import io.horizontalsystems.xrateskit.entities.MarketInfoEntity
import io.reactivex.Single

class BaseMarketInfoProvider(
    private val cryptoCompareProvider: CryptoCompareProvider,
    private val uniswapGraphProvider: UniswapGraphProvider
) : IMarketInfoProvider {

    override fun getMarketInfo(coins: List<Coin>, currency: String): Single<List<MarketInfoEntity>> {

        val (ethBasedCoins, baseCoins) = coins.partition {
                coin -> coin.type is CoinType.Ethereum || coin.type is CoinType.Erc20
        }

        return Single.zip(
            cryptoCompareProvider.getMarketInfo(baseCoins, currency),
            uniswapGraphProvider.getMarketInfo(ethBasedCoins, currency),
            { t1, t2 -> t1 + t2
            })
    }
}
