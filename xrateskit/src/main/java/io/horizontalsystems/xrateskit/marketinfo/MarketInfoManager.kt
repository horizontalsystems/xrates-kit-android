package io.horizontalsystems.xrateskit.marketinfo

import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.MarketInfoKey
import io.horizontalsystems.xrateskit.entities.MarketInfo
import io.horizontalsystems.xrateskit.entities.MarketInfoEntity

class MarketInfoManager(private val storage: IStorage, private val factory: Factory) {

    var listener: Listener? = null

    interface Listener {
        fun onUpdate(marketInfo: MarketInfo, key: MarketInfoKey)
        fun onUpdate(marketInfoMap: Map<String, MarketInfo>, currency: String)
    }

    fun getLastSyncTimestamp(coins: List<String>, currency: String): Long? {
        val rates = storage.getOldMarketInfo(coins, currency)
        if (rates.size != coins.size) {
            return null
        }

        return rates.lastOrNull()?.timestamp
    }

    fun getMarketInfo(coin: String, currency: String): MarketInfo? {
        return storage.getMarketInfo(coin, currency)?.let { factory.createMarketInfo(it) }
    }

    fun notifyExpired(coins: List<String>, currency: String) {
        val entities = storage.getOldMarketInfo(coins, currency)
        notify(entities, currency)
    }

    fun update(marketInfoList: List<MarketInfoEntity>, currency: String) {
        storage.saveMarketInfo(marketInfoList)
        notify(marketInfoList, currency)
    }

    private fun notify(entities: List<MarketInfoEntity>, currency: String) {
        val marketInfoMap = mutableMapOf<String, MarketInfo>()

        entities.forEach { entity ->
            val rateKey = MarketInfoKey(entity.coinCode, entity.currency)

            val marketInfo = factory.createMarketInfo(entity)
            listener?.onUpdate(marketInfo, rateKey)
            marketInfoMap[entity.coinCode] = marketInfo
        }

        listener?.onUpdate(marketInfoMap, currency)
    }
}
