package io.horizontalsystems.xrateskit.coinmarkets

import io.horizontalsystems.xrateskit.core.IDefiMarketsProvider
import io.horizontalsystems.xrateskit.core.IInfoManager
import io.horizontalsystems.xrateskit.entities.CoinMarket
import io.horizontalsystems.xrateskit.entities.DefiTvl
import io.horizontalsystems.xrateskit.entities.TimePeriod
import io.horizontalsystems.xrateskit.providers.coingecko.CoinGeckoProvider
import io.reactivex.Single

class DefiMarketsManager(
    private val coinGeckoProvider: CoinGeckoProvider,
    private val defiMarketsProvider: IDefiMarketsProvider
) : IInfoManager {

    fun getTopDefiMarketsAsync(currency: String, fetchDiffPeriod: TimePeriod, itemsCount: Int): Single<List<CoinMarket>> {
        return coinGeckoProvider.getTopCoinMarketsAsync(currency, fetchDiffPeriod, itemsCount, true)
    }

    fun getTopDefiTvlAsync(currency: String, fetchDiffPeriod: TimePeriod, itemsCount: Int): Single<List<DefiTvl>> {
        return defiMarketsProvider.getTopDefiTvlAsync(currency, fetchDiffPeriod, itemsCount)
    }

    override fun destroy() {
        defiMarketsProvider.destroy()
    }
}
