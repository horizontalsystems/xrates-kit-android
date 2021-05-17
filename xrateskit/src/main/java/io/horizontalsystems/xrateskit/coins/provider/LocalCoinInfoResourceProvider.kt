package io.horizontalsystems.xrateskit.coins.provider

import android.content.Context
import io.horizontalsystems.xrateskit.entities.CoinInfoResource

class LocalCoinInfoResourceProvider(private val context: Context) : CoinInfoResourceProvider {
    private val coinInfoAssetFileName = "coins.json"

    override fun getDataNewerThan(version: Int?): CoinInfoResource? {
        // if version is not null it means the local file has been already parsed before
        if (version != null) return null

        return CoinInfoResource.parseFile(false, context.assets.open(coinInfoAssetFileName))
    }

}
