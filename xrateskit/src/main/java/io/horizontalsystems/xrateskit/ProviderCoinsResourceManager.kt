package io.horizontalsystems.xrateskit

import io.horizontalsystems.xrateskit.entities.ProviderCoinsResource
import io.horizontalsystems.xrateskit.entities.ResourceType
import io.horizontalsystems.xrateskit.storage.Storage

class ProviderCoinsResourceManager(
    private val providerCoinsResourceProvider: ProviderCoinsResourceProvider,
    private val storage: Storage
) {
    fun getNewData() : ProviderCoinsResource? {
        val resourceInfo = storage.getResourceInfo(ResourceType.PROVIDER_COINS)

        return providerCoinsResourceProvider.getDataNewerThan(resourceInfo?.version)
    }

}
