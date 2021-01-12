package io.horizontalsystems.xrateskit.coinmarkets

import io.horizontalsystems.xrateskit.coins.CoinInfoManager
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.ICoinMarketProvider
import io.horizontalsystems.xrateskit.core.IInfoManager
import io.horizontalsystems.xrateskit.entities.Coin
import io.horizontalsystems.xrateskit.entities.TimePeriod
import io.horizontalsystems.xrateskit.entities.CoinMarket
import io.horizontalsystems.xrateskit.entities.CoinType
import io.horizontalsystems.xrateskit.storage.Storage
import io.reactivex.Single

class CoinMarketManager(
    private val coinMarketProvider: ICoinMarketProvider,
    private val defiMarketProvider: ICoinMarketProvider,
    private val coinInfoManager: CoinInfoManager
): IInfoManager {
    fun getTopCoinMarketsAsync(currency: String, fetchDiffPeriod: TimePeriod = TimePeriod.HOUR_24, itemsCount: Int): Single<List<CoinMarket>> {
        return coinMarketProvider
                .getTopCoinMarketsAsync(currency, fetchDiffPeriod, itemsCount)
                .map { topMarkets ->
                    coinInfoManager.identifyCoins(topMarkets.map { it.coin })
                    topMarkets
                }
    }

    fun getTopDefiMarketsAsync(currency: String, fetchDiffPeriod: TimePeriod = TimePeriod.HOUR_24, itemsCount: Int): Single<List<CoinMarket>> {
        return defiMarketProvider.getTopCoinMarketsAsync(currency, fetchDiffPeriod, itemsCount)
                .map { topDefiMarkets ->
                    topDefiMarkets
                }
    }

    fun getCoinMarketsAsync(coins:List<Coin>, currencyCode: String, fetchDiffPeriod: TimePeriod = TimePeriod.HOUR_24): Single<List<CoinMarket>> {
        val (ethBasedCoins, baseCoins) = coins.partition {
            coin -> coin.type is CoinType.Erc20
        }

        return Single.zip(
                coinMarketProvider.getCoinMarketsAsync(baseCoins, currencyCode, fetchDiffPeriod),
                defiMarketProvider.getCoinMarketsAsync(ethBasedCoins, currencyCode, fetchDiffPeriod),
                { t1, t2 -> t1 + t2
                })

    }

    override fun destroy() {
        coinMarketProvider.destroy()
        defiMarketProvider.destroy()
    }
}
