package io.horizontalsystems.xrateskit.entities

import com.eclipsesource.json.JsonValue
import java.math.BigDecimal

class CoinGeckoCoinMarkets(
    val rate: BigDecimal,
    val rateOpenDay: BigDecimal= BigDecimal.ZERO,
    val rateDiffPeriod: Map<TimePeriod, BigDecimal>? = null,

    val rateHigh24h: BigDecimal = BigDecimal.ZERO,
    val rateLow24h: BigDecimal = BigDecimal.ZERO,

    val volume24h: BigDecimal = BigDecimal.ZERO,
    val marketCap: BigDecimal = BigDecimal.ZERO,
    val marketCapDiff24h: BigDecimal = BigDecimal.ZERO,
    val circulatingSupply: BigDecimal = BigDecimal.ZERO,
    val totalSupply: BigDecimal = BigDecimal.ZERO
)

data class CoinGeckoCoinMarketsResponse(
    val coinInfo: CoinGeckoCoinInfo,
    val coinGeckoMarkets: CoinGeckoCoinMarkets
) {

    companion object {

        fun parseData(jsonValue: JsonValue): List<CoinGeckoCoinMarketsResponse> {

            val coinGeckoMarketsResponses = mutableListOf<CoinGeckoCoinMarketsResponse>()

            jsonValue.asArray().forEach { marketData ->
                marketData?.asObject()?.let { element ->

                    val coinId = element.get("id").asString().toUpperCase()
                    val coinCode = element.get("symbol").asString().toUpperCase()
                    val title = element.get("name").asString()

                    val rate = if (element.get("current_price").isNull) BigDecimal.ZERO
                    else element.get("current_price").asDouble().toBigDecimal()

                    val rateOpenDay = if (element.get("price_change_24h").isNull) BigDecimal.ZERO
                    else rate + element.get("price_change_24h").asDouble().toBigDecimal()

                    val marketCap = if (element.get("market_cap") != null){
                        if (element.get("market_cap").isNull) BigDecimal.ZERO
                        else element.get("market_cap").asDouble().toBigDecimal()
                    } else BigDecimal.ZERO

                    val circulatingSupply = if (element.get("circulating_supply")!= null){
                        if (element.get("circulating_supply").isNull) BigDecimal.ZERO
                        else element.get("circulating_supply").asDouble().toBigDecimal()
                    } else BigDecimal.ZERO

                    val volume24h = if (element.get("total_volume") != null) {
                        if (element.get("total_volume").isNull) BigDecimal.ZERO
                        else element.get("total_volume").asDouble().toBigDecimal()
                    } else BigDecimal.ZERO

                    val rateDiffPeriod = mutableMapOf<TimePeriod, BigDecimal>()
                    if (element.get("price_change_percentage_24h") != null) {
                        rateDiffPeriod.put(
                            TimePeriod.HOUR_24,
                            element.get("price_change_percentage_24h").asDouble().toBigDecimal()
                        )
                    }

                    if (element.get("price_change_percentage_1h_in_currency") != null) {
                        rateDiffPeriod.put(
                            TimePeriod.HOUR_1,
                            element.get("price_change_percentage_1h_in_currency").asDouble().toBigDecimal()
                        )
                    }
                    if (element.get("price_change_percentage_7d_in_currency") != null) {
                        rateDiffPeriod.put(
                            TimePeriod.DAY_7,
                            element.get("price_change_percentage_7d_in_currency").asDouble().toBigDecimal()
                        )
                    }
                    if (element.get("price_change_percentage_14d_in_currency") != null) {
                        rateDiffPeriod.put(
                            TimePeriod.DAY_14,
                            element.get("price_change_percentage_14d_in_currency").asDouble().toBigDecimal()
                        )
                    }

                    if (element.get("price_change_percentage_30d_in_currency") != null) {
                        rateDiffPeriod.put(
                            TimePeriod.DAY_30,
                            element.get("price_change_percentage_30d_in_currency").asDouble().toBigDecimal()
                        )
                    }
                    if (element.get("price_change_percentage_200d_in_currency") != null) {
                        rateDiffPeriod.put(
                            TimePeriod.DAY_200,
                            element.get("price_change_percentage_200d_in_currency").asDouble().toBigDecimal()
                        )
                    }
                    if (element.get("price_change_percentage_1y_in_currency") != null) {
                        rateDiffPeriod.put(
                            TimePeriod.YEAR_1,
                            element.get("price_change_percentage_1y_in_currency").asDouble().toBigDecimal()
                        )
                    }

                    coinGeckoMarketsResponses.add(
                        CoinGeckoCoinMarketsResponse(
                            coinInfo = CoinGeckoCoinInfo(coinId, coinCode, title),
                            coinGeckoMarkets = CoinGeckoCoinMarkets(
                                rate = rate,
                                rateOpenDay = rateOpenDay,
                                rateDiffPeriod = rateDiffPeriod,
                                marketCap = marketCap,
                                volume24h = volume24h,
                                circulatingSupply = circulatingSupply
                            )
                        )
                    )
                }
            }

            return coinGeckoMarketsResponses
        }
    }
}

data class CoinGeckoCoinInfo(
    val coinId: String,
    val coinCode: String,
    val title: String,
    val description: String? = null,
    val links: Map<String, String>? = null
) {
    companion object {
        fun parseData(jsonValue: JsonValue): CoinGeckoCoinInfo {

            val links = mutableMapOf<String, String>()
            val element = jsonValue.asObject()
            val linksElement = element.get("links").asObject()
            val coinId = element.get("id").asString().toUpperCase()
            val coinCode = element.get("symbol").asString().toUpperCase()
            val title = element.get("name").asString()
            val description = if(element.get("description") != null){
                                element.get("description").asObject().get("en").asString()
                              } else ""

            if(linksElement.get("homepage") != null) {
                if (!linksElement.get("homepage").asArray().isNull) {
                    links.put("Website", linksElement.get("homepage").asArray()[0].asString())
                }
            }


            return CoinGeckoCoinInfo(
                coinId = coinId,
                coinCode = coinCode,
                title = title,
                description = description,
                links = links
            )
        }
    }
}

data class CoinGeckoCoinMarketDetailsResponse(
    val coinInfo: CoinGeckoCoinInfo,
    val coinGeckoMarkets: CoinGeckoCoinMarkets,
    val rateDiffs: Map<TimePeriod, Map<String, BigDecimal>>
) {
    companion object {
        fun parseData(
            jsonValue: JsonValue,
            currencyCode: String,
            rateDiffCoinCodes: List<String>,
            rateDiffPeriods: List<TimePeriod>
        ): CoinGeckoCoinMarketDetailsResponse {

            val element = jsonValue.asObject().get("market_data").asObject()
            val rateDiffsPeriod = mutableMapOf<TimePeriod, Map<String, BigDecimal>>()
            val rateDiffs = mutableMapOf<String, BigDecimal>()

            val rate = if (element.get("current_price").isNull) BigDecimal.ZERO
            else{
                if(element.get("current_price").asObject().get(currencyCode.toLowerCase()).isNull) BigDecimal.ZERO
                else element.get("current_price").asObject().get(currencyCode.toLowerCase()).asDouble().toBigDecimal()
            }

            val rateHigh24h = if (element.get("high_24h") != null ){
                if (element.get("high_24h").isNull) BigDecimal.ZERO
                else{
                    if(element.get("high_24h").asObject().get(currencyCode.toLowerCase()).isNull) BigDecimal.ZERO
                    else element.get("high_24h").asObject().get(currencyCode.toLowerCase()).asDouble().toBigDecimal()
                }
            } else BigDecimal.ZERO

            val rateLow24h = if (element.get("low_24h") != null){
                if (element.get("low_24h").isNull) BigDecimal.ZERO
                else{
                    if(element.get("low_24h").asObject().get(currencyCode.toLowerCase()).isNull) BigDecimal.ZERO
                    else element.get("low_24h").asObject().get(currencyCode.toLowerCase()).asDouble().toBigDecimal()
                }
            } else BigDecimal.ZERO

            val marketCap = if (element.get("market_cap") != null){
                if (element.get("market_cap").isNull) BigDecimal.ZERO
                else{
                    if(element.get("market_cap").asObject().get(currencyCode.toLowerCase()).isNull) BigDecimal.ZERO
                    else element.get("market_cap").asObject().get(currencyCode.toLowerCase()).asDouble().toBigDecimal()
                }
            } else BigDecimal.ZERO

            val volume24h = if (element.get("total_volume") != null){
                if (element.get("total_volume").isNull) BigDecimal.ZERO
                else{
                    if(element.get("total_volume").asObject().get(currencyCode.toLowerCase()).isNull) BigDecimal.ZERO
                    else element.get("total_volume").asObject().get(currencyCode.toLowerCase()).asDouble().toBigDecimal()
                }
            } else BigDecimal.ZERO

            val circulatingSupply = if (element.get("circulating_supply") != null){
                if (element.get("circulating_supply").isNull) BigDecimal.ZERO
                else element.get("circulating_supply").asDouble().toBigDecimal()
            } else BigDecimal.ZERO

            val totalSupply = if (element.get("total_supply").isNull){
                if (element.get("total_supply").isNull) BigDecimal.ZERO
                else element.get("total_supply").asDouble().toBigDecimal()
            } else BigDecimal.ZERO

            rateDiffPeriods.forEach { period ->

                val diffPeriod = when(period) {
                    TimePeriod.HOUR_1 -> "price_change_1h_in_currency"
                    TimePeriod.HOUR_24 -> "price_change_24h_in_currency"
                    TimePeriod.DAY_7 -> "price_change_7d_in_currency"
                    TimePeriod.DAY_14 -> "price_change_14d_in_currency"
                    TimePeriod.DAY_30 -> "price_change_30d_in_currency"
                    TimePeriod.DAY_200 -> "price_change_200d_in_currency"
                    TimePeriod.YEAR_1 -> "price_change_1y_in_currency"
                    else -> "price_change_24h_in_currency"
                }

                rateDiffCoinCodes.forEach { coinCode ->

                val diff = if (element.get(diffPeriod) != null) {
                                if (element.get(diffPeriod).isNull) BigDecimal.ZERO
                                else{
                                    if(element.get(diffPeriod).asObject().get(coinCode.toLowerCase()).isNull) BigDecimal.ZERO
                                    else element.get(diffPeriod).asObject().get(coinCode.toLowerCase()).asDouble().toBigDecimal()
                                }
                           } else BigDecimal.ZERO
                    rateDiffs.put(coinCode, diff)

                }
                rateDiffsPeriod.put(period, rateDiffs)
            }

            return CoinGeckoCoinMarketDetailsResponse(

                coinInfo = CoinGeckoCoinInfo.parseData(jsonValue),
                coinGeckoMarkets = CoinGeckoCoinMarkets(
                    rate = rate,
                    rateHigh24h = rateHigh24h,
                    rateLow24h = rateLow24h,
                    marketCap = marketCap,
                    volume24h = volume24h,
                    circulatingSupply = circulatingSupply,
                    totalSupply = totalSupply,
                ),
                rateDiffs = rateDiffsPeriod
            )
        }
    }
}