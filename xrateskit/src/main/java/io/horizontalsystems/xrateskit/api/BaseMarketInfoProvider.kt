package io.horizontalsystems.xrateskit.api

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.api.graphproviders.UniswapGraphProvider
import io.horizontalsystems.xrateskit.core.IMarketInfoProvider
import io.horizontalsystems.xrateskit.entities.MarketInfoEntity
import io.reactivex.Single

class BaseMarketInfoProvider(
    private val cryptoCompareProvider: CryptoCompareProvider,
    private val uniswapGraphProvider: UniswapGraphProvider
) : IMarketInfoProvider {

    override fun getMarketInfo(coinTypes: List<CoinType>, currency: String): Single<List<MarketInfoEntity>> {

        val (ethBasedCoins, baseCoins) = coinTypes.partition { type ->
            type is CoinType.Ethereum || type is CoinType.Erc20
        }

        return Single.zip(
            cryptoCompareProvider.getMarketInfo(baseCoins, currency),
            uniswapGraphProvider.getMarketInfo(ethBasedCoins, currency),
            { t1, t2 -> t1 + t2
            })
    }
}
