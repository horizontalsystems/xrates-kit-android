package io.horizontalsystems.xrateskit.marketinfo

import io.horizontalsystems.xrateskit.core.IMarketInfoProvider
import io.horizontalsystems.xrateskit.entities.Coin
import io.horizontalsystems.xrateskit.entities.MarketInfoEntity
import io.reactivex.Single

class MarketInfoSchedulerProvider(
    val retryInterval: Long,
    val expirationInterval: Long,
    private var coins: List<Coin>,
    private val currency: String,
    private val manager: MarketInfoManager,
    private val provider: IMarketInfoProvider) {
    private val coinCodeList = coins.map { coin -> coin.code }

    val lastSyncTimestamp: Long?
        get() = manager.getLastSyncTimestamp(coinCodeList, currency)

    val syncSingle: Single<Unit>
        get() = provider.getMarketInfo(coins, currency)
                .doOnSuccess { rates ->
                    update(rates)
                }
                .map { Unit }

    fun notifyExpiredRates() {
        manager.notifyExpired(coinCodeList, currency)
    }

    private fun update(list: List<MarketInfoEntity>) {

        list.forEach { marketInfoEntity ->
            coins.find { it.code.toUpperCase().contentEquals(marketInfoEntity.coin.toUpperCase()) }?.let {
                marketInfoEntity.coin = it.code
            }
        }

        coins = coins.filter { coin ->
            list.any { it.coin.contentEquals(coin.code) }
        }

        manager.update(list, currency)
    }
}
