package io.horizontalsystems.xrateskit.coinmarkets

import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.ICoinMarketProvider
import io.horizontalsystems.xrateskit.core.IInfoManager
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.Coin
import io.horizontalsystems.xrateskit.entities.TimePeriod
import io.horizontalsystems.xrateskit.entities.CoinMarket
import io.reactivex.Single

class CoinMarketManager(
    private val coinMarketProvider: ICoinMarketProvider,
    private val storage: IStorage,
    private val factory: Factory
): IInfoManager {

    fun getTopCoinMarketsAsync(currency: String, fetchDiffPeriod: TimePeriod = TimePeriod.HOUR_24, itemsCount: Int): Single<List<CoinMarket>> {
        return coinMarketProvider
            .getTopCoinMarketsAsync(currency, fetchDiffPeriod, itemsCount)
            .map { topMarkets ->
                val marketEntityList = topMarkets.map { factory.createMarketInfoEntity(it.coin, it.marketInfo) }
                storage.saveMarketInfo(marketEntityList)
                topMarkets
            }
    }

    fun getCoinMarketsAsync(coins:List<Coin>, currencyCode: String, fetchDiffPeriod: TimePeriod = TimePeriod.HOUR_24): Single<List<CoinMarket>> {
        return coinMarketProvider
            .getCoinMarketsAsync(coins, currencyCode, fetchDiffPeriod)
            .map { markets ->
                val marketEntityList = markets.map { factory.createMarketInfoEntity(it.coin, it.marketInfo) }
                storage.saveMarketInfo(marketEntityList)
                markets
            }
    }

    override fun destroy() {
        coinMarketProvider.destroy()
    }
}
