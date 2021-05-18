package io.horizontalsystems.xrateskit.coins.provider

import io.horizontalsystems.xrateskit.entities.CoinInfoResource
import java.net.URL

class DataProviderCoinsInfoRemote(private val path: String) : DataProvider<CoinInfoResource> {

    override fun getDataNewerThan(version: Int?) = try {
        val inputStream = URL(path).openStream()
        val coinInfoResource = CoinInfoResource.parseFile(false, inputStream)
        if (coinInfoResource.version != version) {
            coinInfoResource
        } else {
            null
        }
    } catch (e: Exception) {
        null
    }
}
