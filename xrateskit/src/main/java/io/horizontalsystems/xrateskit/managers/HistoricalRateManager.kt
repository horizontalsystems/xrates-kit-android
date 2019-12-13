package io.horizontalsystems.xrateskit.managers

import io.horizontalsystems.xrateskit.api.CryptoCompareProvider
import io.horizontalsystems.xrateskit.core.IStorage
import io.reactivex.Single
import java.math.BigDecimal

class HistoricalRateManager(private val storage: IStorage, private val rateProvider: CryptoCompareProvider) {

    fun getHistoricalRate(coin: String, currency: String, timestamp: Long): BigDecimal? {
        return storage.getHistoricalRate(coin, currency, timestamp)?.value
    }

    fun getHistoricalRateFromApi(coin: String, currency: String, timestamp: Long): Single<BigDecimal> {
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
