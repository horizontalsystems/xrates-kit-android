package io.horizontalsystems.xrateskit

import io.horizontalsystems.xrateskit.entities.ProviderCoinsResource
import io.horizontalsystems.xrateskit.entities.ResourceInfo
import io.horizontalsystems.xrateskit.entities.ResourceType
import io.horizontalsystems.xrateskit.storage.Storage

class CoinExternalIdsSyncer(
    private val providerCoinsResourceProvider: ProviderCoinsResourceProvider,
    private val storage: Storage
) {
    fun syncData() {
        val resourceInfo = storage.getResourceInfo(ResourceType.PROVIDER_COINS)

        providerCoinsResourceProvider.getDataNewerThan(resourceInfo?.version)?.let {
            storage.saveProviderCoins(it.providerCoins)
            storage.saveResourceInfo(ResourceInfo(ResourceType.PROVIDER_COINS, it.version))
        }
    }

}
