package io.horizontalsystems.xrateskit.managers

import io.horizontalsystems.xrateskit.api.CryptoCompareProvider
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.MarketStats
import io.reactivex.Single

class MarketStatsManager(private val storage: IStorage, private val statsProvider: CryptoCompareProvider) {

    fun getMarketStats(coin: String, currency: String): Single<MarketStats> {
        storage.getMarketStats(coin, currency)?.let {
            return Single.just(it)
        }

        return statsProvider
                .getMarketStats(coin, currency)
                .doOnSuccess {
                    storage.saveMarketStats(it)
                }
    }
}
