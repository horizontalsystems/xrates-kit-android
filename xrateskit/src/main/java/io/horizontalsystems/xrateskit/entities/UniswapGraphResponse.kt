package io.horizontalsystems.xrateskit.entities

import com.eclipsesource.json.JsonObject
import com.eclipsesource.json.JsonValue
import java.math.BigDecimal
import java.text.ParseException
import java.util.*

data class EthBlocksGraphResponse(val timePeriod: TimePeriod, val blockHeight: Long){
    companion object{
        fun parseData(jsonObject: JsonObject): Map<TimePeriod, Long>{
            val blockData = mutableMapOf<TimePeriod, Long>()
            jsonObject.asObject().get("data").asObject()?.let { data ->

                data.asObject()?.let {  periodData ->
                    periodData.names().forEach { periodName ->
                        periodData.get(periodName).let {
                            blockData[TimePeriod.valueOf(periodName)] = it.asArray().get(0).asObject().get("number").asString().toLong()
                        }
                    }
                }
            }
            return blockData
        }
    }
}

data class UniswapGraphToken(
    var tokenAddress: String,
    var coinCode: String,
    var coinTitle: String,
    var latestRateInETH: BigDecimal,
    var volumeInUSD: BigDecimal = BigDecimal.ZERO,
    var totalLiquidity: BigDecimal = BigDecimal.ZERO){

    companion object{
        fun parseData(jsonValue: JsonValue): UniswapGraphToken{
            val tokenData = jsonValue.asObject()
            val coinId = tokenData.get("id").asString()
            val coinTitle = tokenData.get("name").asString()
            val coinCode = tokenData.get("symbol").asString().toUpperCase(Locale.getDefault())
            val latestRateInETH = if(!tokenData.get("derivedETH").isNull)  tokenData.get("derivedETH").asString().toBigDecimal() else BigDecimal.ZERO
            val volumeInUSD = if(!tokenData.get("tradeVolumeUSD").isNull)  tokenData.get("tradeVolumeUSD").asString().toBigDecimal() else BigDecimal.ZERO
            val totalLiquidity = if(!tokenData.get("totalLiquidity").isNull)  tokenData.get("totalLiquidity").asString().toBigDecimal() else BigDecimal.ZERO

            return UniswapGraphToken(coinId, coinCode, coinTitle, latestRateInETH, volumeInUSD, totalLiquidity)
        }
    }
}


data class UniswapGraphXRatesResponse(
    var coinCode: String,
    var address: String,
    var latestRateInETH: Double,
    var dayOpeningRateInUSD: Double){

    companion object {
        fun parseData(jsonObject: JsonObject): List<UniswapGraphXRatesResponse> {
            val rates = mutableListOf<UniswapGraphXRatesResponse>()
            val data = jsonObject.get("data")?.asObject()

            data?.names()?.forEach { recordName ->

                if(!data.get(recordName).asArray().isEmpty){
                    data.get(recordName)?.asArray()?.get(0)?.let { it ->

                        val openingRate = if(it.asObject().get("priceUSD").isNull) 0.0
                                          else it.asObject().get("priceUSD").asString().toDouble()
                        val tokenData = it.asObject().get("token").asObject()
                        val coinCode = tokenData.get("symbol").asString().toUpperCase(Locale.getDefault())
                        val tokenId = tokenData.get("id").asString()
                        val latestRate = tokenData.get("derivedETH").asString().toDouble()

                        rates.add(UniswapGraphXRatesResponse(coinCode, tokenId, latestRate, openingRate))
                    }
                }
            }

            return rates
        }
    }
}

data class UniswapGraphTokensResponse(
    val tokens: List<UniswapGraphToken>,
    val ethPriceInUSD : BigDecimal){

    companion object {
        fun parseData(jsonObject: JsonObject): UniswapGraphTokensResponse {
            val rates = mutableListOf<UniswapGraphToken>()
            val data = jsonObject.get("data")?.asObject()

            data?.get("tokens")?.asArray()?.let {  tokens ->
                   tokens.forEach {
                       rates.add(UniswapGraphToken.parseData(it))
                   }
            }

            return UniswapGraphTokensResponse(rates, UniswapGraphEthXRateResponse.parseData(jsonObject).rateInUSD)
        }
    }
}


data class UniswapGraphEthXRateResponse(var rateInUSD: BigDecimal){
    companion object {
        fun parseData(jsonObject: JsonObject): UniswapGraphEthXRateResponse {
            if(!jsonObject.get("data").isNull){
                jsonObject.get("data")?.asObject()?.get("bundles")?.let {
                    return UniswapGraphEthXRateResponse(it.asArray()[0].asObject()["ethPriceUSD"].asString().toBigDecimal())
                }
            }

            throw ParseException("Error parsing Uniswap Ethprice data", 0)
        }
    }
}