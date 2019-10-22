package io.horizontalsystems.xrateskit.latestrate

import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.ILatestRateProvider
import io.reactivex.Single
import java.math.BigDecimal

class LatestRateSchedulerProvider(
        val retryInterval: Long,
        val expirationInterval: Long,
        private val coins: List<String>,
        private val currency: String,
        private val factory: Factory,
        private val manager: LatestRateManager,
        private val provider: ILatestRateProvider) {

    val lastSyncTimestamp: Long?
        get() = manager.getLastSyncTimestamp(coins, currency)

    val syncSingle: Single<Unit>
        get() = provider.getLatestRates(coins, currency)
                .doOnSuccess { rates ->
                    update(rates)
                }
                .map { Unit }


    fun notifyExpiredRates() {
        manager.notifyExpiredRates(coins, currency)
    }

    private fun update(data: Map<String, String>) {
        val rates = coins.map { coin ->
            val latestRateValue = data[coin]?.toBigDecimal() ?: BigDecimal.ZERO
            val latestRate = factory.createLatestRate(coin, currency, latestRateValue)

            latestRate
        }

        manager.update(rates)
    }
}
