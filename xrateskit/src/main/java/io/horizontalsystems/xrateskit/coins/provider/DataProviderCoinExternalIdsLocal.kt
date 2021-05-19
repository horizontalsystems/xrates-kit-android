package io.horizontalsystems.xrateskit.coins.provider

import android.content.Context
import io.horizontalsystems.xrateskit.entities.ProviderCoinsResource
import io.horizontalsystems.xrateskit.entities.ResourceInfo

class DataProviderCoinExternalIdsLocal(private val context: Context) : DataProvider<ProviderCoinsResource> {
    private val providerCoinsFileName = "provider.coins.json"

    override fun getDataNewerThan(resourceInfo: ResourceInfo?): Data<ProviderCoinsResource>? {
        // if version is not null it means the local file has been already parsed before
        if (resourceInfo != null) return null

        return Data("ProviderCoinsResourceLocal", ProviderCoinsResource.parseFile(false, context.assets.open(providerCoinsFileName)))
    }
}
