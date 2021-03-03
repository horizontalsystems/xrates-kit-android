package io.horizontalsystems.xrateskit.marketinfo

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.MarketInfoKey
import io.horizontalsystems.xrateskit.entities.MarketInfo
import io.horizontalsystems.xrateskit.entities.MarketInfoEntity

class MarketInfoManager(private val storage: IStorage, private val factory: Factory) {

    var listener: Listener? = null

    interface Listener {
        fun onUpdate(marketInfo: MarketInfo, key: MarketInfoKey)
        fun onUpdate(marketInfoMap: Map<CoinType, MarketInfo>, currency: String)
    }

    fun getLastSyncTimestamp(coinTypes: List<CoinType>, currency: String): Long? {
        val rates = storage.getOldMarketInfo(coinTypes, currency)
        if (rates.size != coinTypes.size) {
            return null
        }

        return rates.lastOrNull()?.timestamp
    }

    fun getMarketInfo(coinType: CoinType, currency: String): MarketInfo? {
        return storage.getMarketInfo(coinType, currency)?.let { factory.createMarketInfo(it) }
    }

    fun notifyExpired(coinTypes: List<CoinType>, currency: String) {
        val entities = storage.getOldMarketInfo(coinTypes, currency)
        notify(entities, currency)
    }

    fun update(marketInfoList: List<MarketInfoEntity>, currency: String) {
        storage.saveMarketInfo(marketInfoList)
        notify(marketInfoList, currency)
    }

    private fun notify(entities: List<MarketInfoEntity>, currency: String) {
        val marketInfoMap = mutableMapOf<CoinType, MarketInfo>()

        entities.forEach { entity ->
            val rateKey = MarketInfoKey(entity.coinType, entity.currencyCode)

            val marketInfo = factory.createMarketInfo(entity)
            listener?.onUpdate(marketInfo, rateKey)
            marketInfoMap[entity.coinType] = marketInfo
        }

        listener?.onUpdate(marketInfoMap, currency)
    }
}
