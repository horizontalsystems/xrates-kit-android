package io.horizontalsystems.xrateskit.coins.provider

import io.horizontalsystems.xrateskit.entities.CoinInfoResource
import java.io.InputStream
import java.net.URL

class RemoteGitHubCoinInfoResourceProvider(private val path: String) : CoinInfoResourceProvider {

    override fun getDataNewerThan(version: Int?): CoinInfoResource? {
        val inputStream: InputStream

        try {
            inputStream = URL(path).openStream()
        } catch (e: Exception) {
            return null
        }

        val coinInfoResource = CoinInfoResource.parseFile(false, inputStream)
        return if (coinInfoResource.version != version) {
            coinInfoResource
        } else {
            null
        }
    }
}
