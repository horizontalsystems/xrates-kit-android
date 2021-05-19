package io.horizontalsystems.xrateskit.coins.provider

import io.horizontalsystems.xrateskit.entities.CoinInfoResource
import io.horizontalsystems.xrateskit.entities.ResourceInfo
import io.horizontalsystems.xrateskit.utils.OkHttpUtils

class DataProviderCoinsInfoRemote(private val path: String) : DataProvider<CoinInfoResource> {

    override fun getDataNewerThan(resourceInfo: ResourceInfo?): Data<CoinInfoResource>? {
        val response = OkHttpUtils.get(path, resourceInfo?.versionId)

        return when (response.code) {
            200 -> response.body?.byteStream()?.use {
                val etag = response.header("etag") ?: ""
                Data(etag, CoinInfoResource.parseFile(false, it))
            }
            else -> null
        }
    }

}
