package io.horizontalsystems.xrateskit.coins.provider

import io.horizontalsystems.xrateskit.entities.ProviderCoinsResource
import io.horizontalsystems.xrateskit.entities.ResourceInfo
import io.horizontalsystems.xrateskit.entities.ResourceType
import io.horizontalsystems.xrateskit.storage.Storage

class CoinExternalIdsSyncer(
    private val dataProvider: DataProvider<ProviderCoinsResource>,
    private val storage: Storage
) {
    fun sync() {
        val resourceInfo = storage.getResourceInfo(ResourceType.PROVIDER_COINS)

        dataProvider.getDataNewerThan(resourceInfo)?.let {
            storage.saveProviderCoins(it.value.providerCoins)
            storage.saveResourceInfo(ResourceInfo(ResourceType.PROVIDER_COINS, it.versionId))
        }
    }

}
