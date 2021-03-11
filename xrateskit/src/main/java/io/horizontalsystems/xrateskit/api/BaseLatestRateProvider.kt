package io.horizontalsystems.xrateskit.api

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.api.graphproviders.UniswapGraphProvider
import io.horizontalsystems.xrateskit.core.ILatestRateProvider
import io.horizontalsystems.xrateskit.entities.LatestRateEntity
import io.reactivex.Single

class BaseLatestRateProvider(
    private val cryptoCompareProvider: CryptoCompareProvider,
    private val uniswapGraphProvider: UniswapGraphProvider
) : ILatestRateProvider {

    override fun getLatestRate(coinTypes: List<CoinType>, currency: String): Single<List<LatestRateEntity>> {

        val (ethBasedCoins, baseCoins) = coinTypes.partition { type ->
            type is CoinType.Ethereum || type is CoinType.Erc20
        }

        return Single.zip(
            cryptoCompareProvider.getLatestRate(baseCoins, currency),
            uniswapGraphProvider.getLatestRate(ethBasedCoins, currency),
            { t1, t2 -> t1 + t2
            })
    }
}
