package io.horizontalsystems.xrateskit.marketinfo

import io.horizontalsystems.xrateskit.core.IMarketInfoProvider
import io.horizontalsystems.xrateskit.entities.MarketInfoEntity
import io.reactivex.Single

class MarketInfoSchedulerProvider(
        val retryInterval: Long,
        val expirationInterval: Long,
        private var coins: List<String>,
        private val currency: String,
        private val manager: MarketInfoManager,
        private val provider: IMarketInfoProvider) {

    val lastSyncTimestamp: Long?
        get() = manager.getLastSyncTimestamp(coins, currency)

    val syncSingle: Single<Unit>
        get() = provider.getMarketInfo(coins, currency)
                .doOnSuccess { rates ->
                    update(rates)
                }
                .map { Unit }

    fun notifyExpiredRates() {
        manager.notifyExpired(coins, currency)
    }

    private fun update(list: List<MarketInfoEntity>) {
        coins = coins.filter { coin ->
            list.any { it.coin == coin }
        }

        manager.update(list, currency)
    }
}
