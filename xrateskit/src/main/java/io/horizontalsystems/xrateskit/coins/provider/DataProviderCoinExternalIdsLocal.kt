package io.horizontalsystems.xrateskit.coins.provider

import android.content.Context
import io.horizontalsystems.xrateskit.entities.ProviderCoinsResource

class DataProviderCoinExternalIdsLocal(private val context: Context) : DataProvider<ProviderCoinsResource> {
    private val providerCoinsFileName = "provider.coins.json"

    override fun getDataNewerThan(version: Int?): ProviderCoinsResource? {
        // if version is not null it means the local file has been already parsed before
        if (version != null) return null

        return ProviderCoinsResource.parseFile(false, context.assets.open(providerCoinsFileName))
    }
}
