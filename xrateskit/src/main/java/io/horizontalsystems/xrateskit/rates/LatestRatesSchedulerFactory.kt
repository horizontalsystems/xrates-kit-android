package io.horizontalsystems.xrateskit.rates

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.core.ILatestRateProvider

class LatestRatesSchedulerFactory(
    private val manager: LatestRatesManager,
    private val provider: ILatestRateProvider,
    private val expirationInterval: Long,
    private val retryInterval: Long) {

    fun getScheduler(coinType: List<CoinType>, currency: String): LatestRatesScheduler {
        return LatestRatesScheduler(LatestRatesSchedulerProvider(
                retryInterval,
                expirationInterval,
                coinType,
                currency,
                manager,
                provider
        ))
    }
}
