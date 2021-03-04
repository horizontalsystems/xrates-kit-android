package io.horizontalsystems.xrateskit.entities

import com.eclipsesource.json.JsonObject
import io.horizontalsystems.xrateskit.api.ProviderError

class CryptoCompareResponse {

    companion object{
        fun parseData(response: JsonObject): JsonObject {
            val type = response["Type"].asInt()
            when {
                type == 99 -> throw ProviderError.ApiRequestLimitExceeded()
                type == 2 -> throw ProviderError.NoDataForCoin()
                type != 100 -> throw ProviderError.UnknownTypeError()
                else -> return response["Data"].asObject()
            }
        }
    }
}