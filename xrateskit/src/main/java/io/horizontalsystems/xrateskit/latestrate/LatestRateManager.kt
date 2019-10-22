package io.horizontalsystems.xrateskit.latestrate

import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.LatestRate
import io.horizontalsystems.xrateskit.entities.LatestRateKey
import io.horizontalsystems.xrateskit.entities.RateInfo

class LatestRateManager(private val storage: IStorage, private val factory: Factory) {

    var listener: Listener? = null

    interface Listener {
        fun onUpdate(rate: RateInfo, key: LatestRateKey)
    }

    fun getLastSyncTimestamp(coins: List<String>, currency: String): Long? {
        val rates = storage.getOldLatestRates(coins, currency)
        if (rates.size != coins.size) {
            return null
        }

        return rates.lastOrNull()?.timestamp
    }

    fun getLatestRate(coin: String, currency: String): RateInfo? {
        return storage.getLatestRate(coin, currency)?.let { factory.createRateInfo(it) }
    }

    fun notifyExpiredRates(coins: List<String>, currency: String) {
        val rates = storage.getOldLatestRates(coins, currency)
        notify(rates)
    }

    fun update(rates: List<LatestRate>) {
        storage.saveLatestRates(rates)
        notify(rates)
    }

    private fun notify(rates: List<LatestRate>) {
        rates.forEach { rate ->
            val rateInfo = factory.createRateInfo(rate)
            val rateKey = LatestRateKey(rate.coin, rate.currency)

            listener?.onUpdate(rateInfo, rateKey)
        }
    }
}
