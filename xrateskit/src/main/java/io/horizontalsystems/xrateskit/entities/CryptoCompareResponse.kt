package io.horizontalsystems.xrateskit.entities

import com.eclipsesource.json.JsonObject

class CryptoCompareResponse {

    companion object{
        fun parseData(response: JsonObject): JsonObject {
            val type = response["Type"].asInt()
            when {
                type == 99 -> throw CryptoCompareError.ApiRequestLimitExceeded()
                type == 2 -> throw CryptoCompareError.NoDataForCoin()
                type != 100 -> throw CryptoCompareError.UnknownTypeError()
                else -> return response["Data"].asObject()
            }
        }
    }
}

sealed class CryptoCompareError: Exception() {
    class ApiRequestLimitExceeded : CryptoCompareError()
    class NoDataForCoin : CryptoCompareError()
    class UnknownTypeError : CryptoCompareError()
}
