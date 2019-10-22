package io.horizontalsystems.xrateskit.managers

import io.horizontalsystems.xrateskit.api.CryptoCompareProvider
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.MarketStats
import io.horizontalsystems.xrateskit.entities.MarketStatsInfo
import io.reactivex.Single
import java.util.*

class MarketStatsManager(
        private val storage: IStorage,
        private val factory: Factory,
        private val statsProvider: CryptoCompareProvider) {

    private val statsExpirationTime = 24 * 60 * 60 // 24 hour

    fun getMarketStats(coin: String, currency: String): Single<MarketStatsInfo> {

        val marketStats = storage.getMarketStats(coin, currency)
        if (marketStats != null && !isExpired(marketStats)) {
            return Single.just(factory.createMarketCapInfo(marketStats))
        }

        return statsProvider
                .getMarketStats(coin, currency)
                .doOnSuccess {
                    storage.saveMarketStats(it)
                }.map {
                    factory.createMarketCapInfo(it)
                }
    }

    private fun isExpired(marketStats: MarketStats): Boolean {
        return marketStats.timestamp < Date().time / 1000 - statsExpirationTime
    }
}
