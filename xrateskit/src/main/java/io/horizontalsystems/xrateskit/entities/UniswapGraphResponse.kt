package io.horizontalsystems.xrateskit.entities

import com.eclipsesource.json.JsonObject
import java.text.ParseException
import java.util.*

data class UniswapGraphXRatesResponse(
    var coinCode: String,
    var latestRateInETH: Double,
    var dayOpeningRateInUSD: Double){

    companion object {
        fun parseData(jsonObject: JsonObject): List<UniswapGraphXRatesResponse> {
            val rates = mutableListOf<UniswapGraphXRatesResponse>()
            val data = jsonObject.get("data")?.asObject()

            data?.names()?.forEach { recordName ->

                if(!data.get(recordName).asArray().isEmpty){
                    data.get(recordName)?.asArray()?.get(0)?.let { it ->

                        val openingRate = it.asObject().get("priceUSD").asString().toDouble()
                        val tokenData = it.asObject().get("token").asObject()
                        val coinCode = tokenData.get("symbol").asString().toUpperCase(Locale.getDefault())
                        val latestRate = tokenData.get("derivedETH").asString().toDouble()

                        rates.add(UniswapGraphXRatesResponse(coinCode, latestRate, openingRate))
                    }
                }
            }

            return rates
        }
    }
}

data class UniswapGraphEthXRateResponse(var rateInUSD: Double){
    companion object {
        fun parseData(jsonObject: JsonObject): UniswapGraphEthXRateResponse {
            if(!jsonObject.get("data").isNull){
                jsonObject.get("data")?.asObject()?.get("bundle")?.let {
                    return UniswapGraphEthXRateResponse(it.asObject()["ethPriceUSD"].asString().toDouble())
                }
            }

            throw ParseException("Error parsing Uniswap Ethprice data", 0)
        }
    }
}