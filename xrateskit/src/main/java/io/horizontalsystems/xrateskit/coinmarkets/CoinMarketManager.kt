package io.horizontalsystems.xrateskit.coinmarkets

import io.horizontalsystems.xrateskit.core.ICoinMarketProvider
import io.horizontalsystems.xrateskit.core.IInfoManager
import io.horizontalsystems.xrateskit.entities.Coin
import io.horizontalsystems.xrateskit.entities.TimePeriod
import io.horizontalsystems.xrateskit.entities.CoinMarket
import io.reactivex.Single

class CoinMarketManager(private val coinMarketProvider: ICoinMarketProvider): IInfoManager {
    fun getTopCoinMarketsAsync(currency: String, fetchDiffPeriod: TimePeriod = TimePeriod.HOUR_24, itemsCount: Int): Single<List<CoinMarket>> {
        return coinMarketProvider
                .getTopCoinMarketsAsync(currency, fetchDiffPeriod, itemsCount)
                .map { topMarkets ->
                    topMarkets
                }
    }

    fun getCoinMarketsAsync(coins:List<Coin>, currencyCode: String, fetchDiffPeriod: TimePeriod = TimePeriod.HOUR_24): Single<List<CoinMarket>> {
        return coinMarketProvider.getCoinMarketsAsync(coins, currencyCode, fetchDiffPeriod)
    }

    override fun destroy() {
        coinMarketProvider.destroy()
    }
}
