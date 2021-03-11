package io.horizontalsystems.xrateskit.rates

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.LatestRate
import io.horizontalsystems.xrateskit.entities.LatestRateKey
import io.horizontalsystems.xrateskit.entities.LatestRateEntity

class LatestRatesManager(private val storage: IStorage, private val factory: Factory) {

    var listener: Listener? = null

    interface Listener {
        fun onUpdate(LatestRate: LatestRate, key: LatestRateKey)
        fun onUpdate(LatestRateMap: Map<CoinType, LatestRate>, currency: String)
    }

    fun getLastSyncTimestamp(coinTypes: List<CoinType>, currency: String): Long? {
        val rates = storage.getOldLatestRates(coinTypes, currency)
        if (rates.size != coinTypes.size) {
            return null
        }

        return rates.lastOrNull()?.timestamp
    }

    fun getLatestRate(coinType: CoinType, currency: String): LatestRate? {
        return storage.getLatestRate(coinType, currency)?.let { factory.createLatestRate(it) }
    }

    fun notifyExpired(coinTypes: List<CoinType>, currency: String) {
        val entities = storage.getOldLatestRates(coinTypes, currency)
        notify(entities, currency)
    }

    fun update(latestRateList: List<LatestRateEntity>, currency: String) {
        storage.saveLatestRates(latestRateList)
        notify(latestRateList, currency)
    }

    private fun notify(entities: List<LatestRateEntity>, currency: String) {
        val latestRateMap = mutableMapOf<CoinType, LatestRate>()

        entities.forEach { entity ->
            val rateKey = LatestRateKey(entity.coinType, entity.currencyCode)

            val latestRate = factory.createLatestRate(entity)
            listener?.onUpdate(latestRate, rateKey)
            latestRateMap[entity.coinType] = latestRate
        }

        listener?.onUpdate(latestRateMap, currency)
    }
}
