package io.horizontalsystems.xrateskit.rates

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.core.ILatestRateProvider
import io.horizontalsystems.xrateskit.entities.LatestRateEntity
import io.reactivex.Single

class LatestRatesSchedulerProvider(
    val retryInterval: Long,
    val expirationInterval: Long,
    private var coinTypes: List<CoinType>,
    private val currency: String,
    private val manager: LatestRatesManager,
    private val provider: ILatestRateProvider) {

    val lastSyncTimestamp: Long?
        get() = manager.getLastSyncTimestamp(coinTypes, currency)

    val syncSingle: Single<Unit>
        get() = provider.getLatestRate(coinTypes, currency)
                .doOnSuccess { rates ->
                    update(rates)
                }
                .map { Unit }

    fun notifyExpiredRates() {
        manager.notifyExpired(coinTypes, currency)
    }

    private fun update(list: List<LatestRateEntity>) {

        list.forEach { latestRateEntity ->
            coinTypes.find { it.ID.toUpperCase().contentEquals(latestRateEntity.coinType.ID.toUpperCase()) }?.let {
                latestRateEntity.coinType = it
            }
        }

        coinTypes = coinTypes.filter { coinType ->
            list.any { it.coinType.ID.contentEquals(coinType.ID) }
        }

        manager.update(list, currency)
    }
}
