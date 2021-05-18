package io.horizontalsystems.xrateskit.coins.provider

import io.horizontalsystems.xrateskit.entities.ProviderCoinsResource
import java.net.URL

class DataProviderCoinExternalIdsRemote(private val path: String) : DataProvider<ProviderCoinsResource> {

    override fun getDataNewerThan(version: Int?) = try {
        val inputStream = URL(path).openStream()
        val coinInfoResource = ProviderCoinsResource.parseFile(false, inputStream)
        if (coinInfoResource.version != version) {
            coinInfoResource
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}
