package io.horizontalsystems.xrateskit.coins.provider

import io.horizontalsystems.xrateskit.entities.CoinInfoResource
import io.horizontalsystems.xrateskit.entities.ResourceType
import io.horizontalsystems.xrateskit.storage.Storage

class CoinInfoResourceManager(
    private val coinInfoResourceProvider: CoinInfoResourceProviderImpl,
    private val storage: Storage
) {
    fun getNewData(): CoinInfoResource? {
        val resourceInfo = storage.getResourceInfo(ResourceType.COIN_INFO)

        return coinInfoResourceProvider.getDataNewerThan(resourceInfo?.version)
    }
}
