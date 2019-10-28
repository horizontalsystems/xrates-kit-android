package io.horizontalsystems.xrateskit.marketinfo

import io.horizontalsystems.xrateskit.core.IMarketInfoProvider

class MarketInfoSchedulerFactory(
        private val manager: MarketInfoManager,
        private val provider: IMarketInfoProvider,
        private val expirationInterval: Long,
        private val retryInterval: Long) {

    fun getScheduler(coins: List<String>, currency: String): MarketInfoScheduler {
        return MarketInfoScheduler(MarketInfoSchedulerProvider(
                retryInterval,
                expirationInterval,
                coins,
                currency,
                manager,
                provider
        ))
    }
}
