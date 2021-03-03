package io.horizontalsystems.xrateskit.marketinfo

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.core.IMarketInfoProvider

class MarketInfoSchedulerFactory(
        private val manager: MarketInfoManager,
        private val provider: IMarketInfoProvider,
        private val expirationInterval: Long,
        private val retryInterval: Long) {

    fun getScheduler(coinType: List<CoinType>, currency: String): MarketInfoScheduler {
        return MarketInfoScheduler(MarketInfoSchedulerProvider(
                retryInterval,
                expirationInterval,
                coinType,
                currency,
                manager,
                provider
        ))
    }
}
