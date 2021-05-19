package io.horizontalsystems.xrateskit.coins.provider

import android.content.Context
import io.horizontalsystems.xrateskit.entities.CoinInfoResource
import io.horizontalsystems.xrateskit.entities.ResourceInfo

class DataProviderCoinsInfoLocal(private val context: Context) : DataProvider<CoinInfoResource> {
    private val coinInfoAssetFileName = "coins.json"

    override fun getDataNewerThan(resourceInfo: ResourceInfo?): Data<CoinInfoResource>? {
        // if version is not null it means the local file has been already parsed before
        if (resourceInfo != null) return null

        return Data("CoinInfoResourceLocal", CoinInfoResource.parseFile(false, context.assets.open(coinInfoAssetFileName)))
    }

}
