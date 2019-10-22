package io.horizontalsystems.xrateskit.latestrate

import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.ILatestRateProvider

class LatestRateSchedulerFactory(
        private val factory: Factory,
        private val manager: LatestRateManager,
        private val provider: ILatestRateProvider,
        private val expirationInterval: Long,
        private val retryInterval: Long) {

    fun getScheduler(coins: List<String>, currency: String): LatestRateScheduler {
        return LatestRateScheduler(LatestRateSchedulerProvider(
                retryInterval,
                expirationInterval,
                coins,
                currency,
                factory,
                manager,
                provider
        ))
    }
}
