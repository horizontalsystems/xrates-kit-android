package io.horizontalsystems.xrateskit.managers

import io.horizontalsystems.xrateskit.api.CryptoCompareProvider
import io.horizontalsystems.xrateskit.core.IStorage
import io.reactivex.Single
import java.math.BigDecimal

class HistoricalRateManager(private val storage: IStorage, private val rateProvider: CryptoCompareProvider) {

    fun getHistoricalRate(coin: String, currency: String, timestamp: Long): Single<BigDecimal> {
        storage.getHistoricalRate(coin, currency, timestamp)?.let {
            return Single.just(it.value)
        }

        return rateProvider
                .getHistoricalRate(coin, currency, timestamp)
                .doOnSuccess {
                    storage.saveHistoricalRate(it)
                }
                .map {
                    it.value
                }
    }
}
