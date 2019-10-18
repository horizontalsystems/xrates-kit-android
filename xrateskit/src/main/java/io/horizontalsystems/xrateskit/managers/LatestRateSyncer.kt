package io.horizontalsystems.xrateskit.managers

import io.horizontalsystems.xrateskit.XRatesDataSource
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.ILatestRateProvider
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.LatestRate
import io.reactivex.Single
import java.math.BigDecimal

class LatestRateSyncer(
        private val factory: Factory,
        private val storage: IStorage,
        private val dataSource: XRatesDataSource,
        private val rateProvider: ILatestRateProvider) {

    var listener: Listener? = null

    interface Listener {
        fun onUpdate(latestRate: LatestRate)
    }

    val lastSyncTimestamp: Long?
        get() = lastSyncTimestamp()

    val syncSingle: Single<Unit>
        get() = sync()

    fun notifyExpiredRates() {
        storage.getOldLatestRates(dataSource.coins, dataSource.currency).forEach {
            listener?.onUpdate(it)
        }
    }

    private fun sync(): Single<Unit> {
        if (dataSource.coins.isEmpty() || dataSource.currency == "") {
            return Single.just(Unit)
        }

        return rateProvider
                .getLatestRate(dataSource.coins, dataSource.currency)
                .doOnSuccess { updateRates(it) }
                .map { Unit }
    }

    private fun updateRates(data: Map<String, String>) {
        val rates = dataSource.coins.map { coin ->
            val latestRateValue = data[coin]?.toBigDecimal() ?: BigDecimal.ZERO
            val latestRate = factory.createLatestRate(coin, dataSource.currency, latestRateValue)

            listener?.onUpdate(latestRate)
            latestRate
        }

        storage.saveLatestRates(rates)
    }

    private fun lastSyncTimestamp(): Long? {
        val latestRates = storage.getOldLatestRates(dataSource.coins, dataSource.currency)
        if (latestRates.size != dataSource.coins.size) {
            return null
        }

        return latestRates.lastOrNull()?.timestamp
    }
}
