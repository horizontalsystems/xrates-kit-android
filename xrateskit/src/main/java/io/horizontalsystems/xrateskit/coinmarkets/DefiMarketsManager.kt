package io.horizontalsystems.xrateskit.coinmarkets

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.core.IDefiMarketsProvider
import io.horizontalsystems.xrateskit.core.IInfoManager
import io.horizontalsystems.xrateskit.entities.CoinMarket
import io.horizontalsystems.xrateskit.entities.DefiTvl
import io.horizontalsystems.xrateskit.entities.DefiTvlPoint
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

    fun getTopDefiTvlAsync(currency: String, fetchDiffPeriod: TimePeriod, itemsCount: Int, chain: String?): Single<List<DefiTvl>> {
        return defiMarketsProvider.getTopDefiTvlAsync(currency, fetchDiffPeriod, itemsCount, chain)
    }

    fun getDefiTvlPointsAsync(coinType: CoinType, currency: String, fetchDiffPeriod: TimePeriod): Single<List<DefiTvlPoint>> {
        return defiMarketsProvider.getDefiTvlPointsAsync(coinType, currency, fetchDiffPeriod)
    }

    fun getDefiTvlAsync(coinType: CoinType, currency: String): Single<DefiTvl> {
        return defiMarketsProvider.getDefiTvlAsync(coinType, currency)
    }

    override fun destroy() {
        defiMarketsProvider.destroy()
    }
}
