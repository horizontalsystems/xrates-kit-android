package io.horizontalsystems.xrateskit.marketinfo

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.core.IMarketInfoProvider
import io.horizontalsystems.xrateskit.entities.MarketInfoEntity
import io.reactivex.Single

class MarketInfoSchedulerProvider(
    val retryInterval: Long,
    val expirationInterval: Long,
    private var coinTypes: List<CoinType>,
    private val currency: String,
    private val manager: MarketInfoManager,
    private val provider: IMarketInfoProvider) {

    val lastSyncTimestamp: Long?
        get() = manager.getLastSyncTimestamp(coinTypes, currency)

    val syncSingle: Single<Unit>
        get() = provider.getMarketInfo(coinTypes, currency)
                .doOnSuccess { rates ->
                    update(rates)
                }
                .map { Unit }

    fun notifyExpiredRates() {
        manager.notifyExpired(coinTypes, currency)
    }

    private fun update(list: List<MarketInfoEntity>) {

        list.forEach { marketInfoEntity ->
            coinTypes.find { it.ID.toUpperCase().contentEquals(marketInfoEntity.coinType.ID.toUpperCase()) }?.let {
                marketInfoEntity.coinType = it
            }
        }

        coinTypes = coinTypes.filter { coinType ->
            list.any { it.coinType.ID.contentEquals(coinType.ID) }
        }

        manager.update(list, currency)
    }
}
