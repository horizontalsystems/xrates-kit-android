package io.horizontalsystems.xrateskit.rates

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.api.CryptoCompareProvider
import io.horizontalsystems.xrateskit.core.IStorage
import io.reactivex.Single
import java.math.BigDecimal

class HistoricalRateManager(private val storage: IStorage, private val rateProvider: CryptoCompareProvider) {

    fun getHistoricalRate(coinType: CoinType, currency: String, timestamp: Long): BigDecimal? {
        return storage.getHistoricalRate(coinType, currency, timestamp)?.value
    }

    fun getHistoricalRateFromApi(coinType: CoinType, currency: String, timestamp: Long): Single<BigDecimal> {
        return rateProvider
                .getHistoricalRate(coinType, currency, timestamp)
                .doOnSuccess {
                    storage.saveHistoricalRate(it)
                }
                .map {
                    it.value
                }
    }
}
