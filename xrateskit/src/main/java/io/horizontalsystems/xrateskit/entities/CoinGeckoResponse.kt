package io.horizontalsystems.xrateskit.entities

import com.eclipsesource.json.JsonValue
import java.lang.Exception
import java.lang.Math.abs
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

                    val coinId = element.get("id").asString()
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
                        if (!element.get("price_change_percentage_24h").isNull)
                            rateDiffPeriod.put(
                                TimePeriod.HOUR_24,
                                element.get("price_change_percentage_24h").asDouble().toBigDecimal()
                            )
                    }
                    if (element.get("price_change_percentage_1h_in_currency") != null) {
                        if (!element.get("price_change_percentage_1h_in_currency").isNull)
                            rateDiffPeriod.put(
                                TimePeriod.HOUR_1,
                                element.get("price_change_percentage_1h_in_currency").asDouble().toBigDecimal()
                            )
                    }
                    if (element.get("price_change_percentage_7d_in_currency") != null) {
                        if (!element.get("price_change_percentage_7d_in_currency").isNull)
                            rateDiffPeriod.put(
                                TimePeriod.DAY_7,
                                element.get("price_change_percentage_7d_in_currency").asDouble().toBigDecimal()
                            )
                    }
                    if (element.get("price_change_percentage_14d_in_currency") != null) {
                        if (!element.get("price_change_percentage_14d_in_currency").isNull)
                            rateDiffPeriod.put(
                                TimePeriod.DAY_14,
                                element.get("price_change_percentage_14d_in_currency").asDouble().toBigDecimal()
                            )
                    }

                    if (element.get("price_change_percentage_30d_in_currency") != null) {
                        if (!element.get("price_change_percentage_30d_in_currency").isNull)
                            rateDiffPeriod.put(
                                TimePeriod.DAY_30,
                                element.get("price_change_percentage_30d_in_currency").asDouble().toBigDecimal()
                            )
                    }
                    if (element.get("price_change_percentage_200d_in_currency") != null) {
                        if (!element.get("price_change_percentage_200d_in_currency").isNull)
                            rateDiffPeriod.put(
                                TimePeriod.DAY_200,
                                element.get("price_change_percentage_200d_in_currency").asDouble().toBigDecimal()
                            )
                    }
                    if (element.get("price_change_percentage_1y_in_currency") != null) {
                        if (!element.get("price_change_percentage_1y_in_currency").isNull)
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
    val description: String = "",
    val links: Map<LinkType, String> = emptyMap(),
    val platforms: Map<CoinPlatformType, String> = emptyMap(),
    val tickers: List<CoinGeckoTickersResponse> = emptyList()
) {
    companion object {
        fun parseData(jsonValue: JsonValue): CoinGeckoCoinInfo {

            val links = mutableMapOf<LinkType, String>()
            val platforms = mutableMapOf<CoinPlatformType, String>()
            val element = jsonValue.asObject()
            val coinId = element.get("id").asString()
            val coinCode = element.get("symbol").asString().toUpperCase()
            val title = element.get("name").asString()
            val description = if (element.get("description") != null) {
                element.get("description").asObject().get("en").asString()
            } else ""

            try {

                element.get("links")?.let {
                    it.asObject().get("homepage")?.let {
                        if (!it.asArray().isNull) {
                            links[LinkType.WEBSITE] = it.asArray()[0].asString()
                        }
                    }

                    it.asObject().get("twitter_screen_name")?.let {
                        if (!it.isNull) {
                            links[LinkType.TWITTER] = "https://twitter.com/${it.asString()}"
                        }
                    }

                    it.asObject().get("telegram_channel_identifier")?.let {
                        if (!it.isNull) {
                            links[LinkType.TELEGRAM] = "https://t.me/${it.asString()}"
                        }
                    }

                    it.asObject().get("subreddit_url")?.let {
                        if (!it.isNull) {
                            links[LinkType.REDDIT] = it.asString()
                        }
                    }

                    it.asObject().get("repos_url")?.let {
                        it.asObject().get("github")?.let { github ->
                            if (!github.asArray().isEmpty)
                                links[LinkType.GITHUB] = github.asArray()[0].asString()
                        }
                    }
                }

                element.get("platforms")?.let {
                    it.asObject()?.let {
                        it.asObject().forEach { platform ->
                            val platformId = platform.name
                            val platformType = when(platformId.toLowerCase()){
                                "tron" ->  CoinPlatformType.TRON
                                "ethereum" ->  CoinPlatformType.ETHEREUM
                                "eos" ->  CoinPlatformType.EOS
                                "binance-smart-chain" ->  CoinPlatformType.BINANCE_SMART_CHAIN
                                "binancecoin" ->  CoinPlatformType.BINANCE
                                else -> CoinPlatformType.OTHER
                            }

                            platform.value?.let {
                                if(!it.isNull){
                                    platforms[platformType] = it.asString()
                                }
                            }
                        }
                    }
                }

            } catch (e: Exception){
                print(e.getLocalizedMessage())    //ignore error
            }

            return CoinGeckoCoinInfo(
                coinId = coinId,
                coinCode = coinCode,
                title = title,
                description = description,
                links = links,
                platforms = platforms,
                tickers = CoinGeckoTickersResponse.parseData(jsonValue, coinCode, platforms.map { it.value.toLowerCase() })
            )
        }
    }
}

data class CoinGeckoTickersResponse(
    val base: String,
    val target: String,
    val marketName: String,
    val marketId: String,
    val rate: BigDecimal = BigDecimal.ZERO,
    val volume: BigDecimal = BigDecimal.ZERO){

    companion object {
        fun parseData(jsonValue: JsonValue, coinCode: String, contractAddresses: List<String>): List<CoinGeckoTickersResponse> {
            val tickers = mutableListOf<CoinGeckoTickersResponse>()
            try{

                jsonValue.asObject().get("tickers")?.let {

                    it.asArray().forEach { tickerData ->
                        tickerData?.asObject()?.let { element ->
                            var base = element.get("base").asString()
                            var target = element.get("target").asString()

                            if(contractAddresses.isNotEmpty()){
                                if(contractAddresses.contains(base.toLowerCase()))
                                    base = coinCode.toUpperCase()
                                else if(contractAddresses.contains(target.toLowerCase()))
                                    target = coinCode.toUpperCase()
                            }

                            val marketName =
                                if(element.get("market") != null){
                                    element.get("market").asObject().get("name").asString()
                                } else ""

                            val marketId =
                                if(element.get("market") != null){
                                    element.get("market").asObject().get("identifier").asString()
                                } else ""

                            val rate =
                                if(!element.get("last").isNull)
                                    element.get("last").asDouble().toBigDecimal()
                                else BigDecimal.ZERO

                            val volume =
                                if(!element.get("volume").isNull)
                                    element.get("volume").asDouble().toBigDecimal()
                                else BigDecimal.ZERO

                            tickers.add(CoinGeckoTickersResponse(base, target, marketName, marketId, rate, volume))
                        }
                    }
                }

            } catch(e: Exception){
                //ignore
            }

            return tickers
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

            val rate = if (element.get("current_price").isNull) BigDecimal.ZERO
            else{
                element.get("current_price").asObject().get(currencyCode.toLowerCase())?.let {
                    if(it.isNull) BigDecimal.ZERO
                    else it.asDouble().toBigDecimal()
                } ?:BigDecimal.ZERO
            }

            val rateHigh24h = element.get("high_24h")?.let { highPriceElement ->
                if (highPriceElement.isNull) BigDecimal.ZERO
                else{
                    highPriceElement.asObject().get(currencyCode.toLowerCase())?.let {
                        if(it.isNull) BigDecimal.ZERO
                        else it.asDouble().toBigDecimal()
                    } ?:BigDecimal.ZERO
                }
            } ?: BigDecimal.ZERO

            val rateLow24h = element.get("low_24h")?.let { lowPriceElement ->
                if (lowPriceElement.isNull) BigDecimal.ZERO
                else{
                    lowPriceElement.asObject().get(currencyCode.toLowerCase())?.let {
                        if(it.isNull) BigDecimal.ZERO
                        else it.asDouble().toBigDecimal()
                    } ?:BigDecimal.ZERO
                }
            } ?: BigDecimal.ZERO

            val marketCap = element.get("market_cap")?.let { marketElement ->
                if (marketElement.isNull) BigDecimal.ZERO
                else{
                    marketElement.asObject().get(currencyCode.toLowerCase())?.let {
                        if(it.isNull) BigDecimal.ZERO
                        else it.asDouble().toBigDecimal()
                    } ?:BigDecimal.ZERO
                }
            } ?: BigDecimal.ZERO

            val volume24h = element.get("total_volume")?.let { volumeElement ->
                if (volumeElement.isNull) BigDecimal.ZERO
                else{
                    volumeElement.asObject().get(currencyCode.toLowerCase())?.let {
                        if(it.isNull) BigDecimal.ZERO
                        else it.asDouble().toBigDecimal()
                    } ?:BigDecimal.ZERO
                }
            } ?: BigDecimal.ZERO

            val circulatingSupply = element.get("circulating_supply")?.let {
                if (it.isNull) BigDecimal.ZERO
                else it.asDouble().toBigDecimal()
            } ?: BigDecimal.ZERO

            val totalSupply = element.get("total_supply")?.let {
                if (it.isNull) BigDecimal.ZERO
                else it.asDouble().toBigDecimal()
            } ?: BigDecimal.ZERO

            rateDiffPeriods.forEach { period ->

                val rateDiffs = mutableMapOf<String, BigDecimal>()
                val diffPeriod = when(period) {
                    TimePeriod.HOUR_1 -> "price_change_percentage_1h_in_currency"
                    TimePeriod.HOUR_24 -> "price_change_percentage_24h_in_currency"
                    TimePeriod.DAY_7 -> "price_change_percentage_7d_in_currency"
                    TimePeriod.DAY_14 -> "price_change_percentage_14d_in_currency"
                    TimePeriod.DAY_30 -> "price_change_percentage_30d_in_currency"
                    TimePeriod.DAY_200 -> "price_change_percentage_200d_in_currency"
                    TimePeriod.YEAR_1 -> "price_change_percentage_1y_in_currency"
                    else -> "price_change_percentage_24h_in_currency"
                }

                rateDiffCoinCodes.forEach { coinCode ->
                    val diff = element.get(diffPeriod)?.let {
                        if (it.isNull) BigDecimal.ZERO
                        else{
                            it.asObject().get(coinCode.toLowerCase())?.let {
                                if (it.isNull) BigDecimal.ZERO
                                else it.asDouble().toBigDecimal()
                            } ?: BigDecimal.ZERO
                        }
                    } ?: BigDecimal.ZERO
                    rateDiffs[coinCode] = diff
                }
                rateDiffsPeriod[period] = rateDiffs
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

class CoinGeckoHistoRateResponse(
    val timeDiff: Long,
    val rate: BigDecimal) {

    companion object {
        fun parseData(jsonValue: JsonValue, timestamp: Long): List<CoinGeckoHistoRateResponse> {

            val ratesArray = jsonValue.asObject().get("prices").asArray()
            val rates = mutableListOf<CoinGeckoHistoRateResponse>()

            ratesArray?.let {
                ratesArray.forEach {

                    val timeDiff = kotlin.math.abs((it.asArray()[0].asLong() / 1000) - timestamp)
                    rates.add(CoinGeckoHistoRateResponse(timeDiff, it.asArray()[1].asDouble().toBigDecimal()))
                }
            }

            return rates
        }
    }
}

class CoinGeckoMarketChartsResponse(
    val rate: BigDecimal,
    val volume: BigDecimal,
    val timestamp: Long
) {
    companion object{
        fun parseData(chartPointKey: ChartInfoKey, jsonValue: JsonValue): List<CoinGeckoMarketChartsResponse> {
            val charts = mutableListOf<CoinGeckoMarketChartsResponse>()

            val rates = jsonValue.asObject().get("prices").asArray()
            val volumes = jsonValue.asObject().get("total_volumes").asArray()
            var nextTs = 0L
            val chartPointsCount = chartPointKey.chartType.interval * 2

            rates.forEachIndexed { index, rateData ->
                try {
                    val timestamp = rateData.asArray()[0].asLong()/1000

                    if(timestamp >= nextTs || rates.size() <= chartPointsCount){
                        nextTs = timestamp + chartPointKey.chartType.seconds - 180
                        val rate = rateData.asArray()[1].asDouble().toBigDecimal()
                        val volume =
                            if(chartPointKey.chartType.days >= 90) volumes[index].asArray()[1].asDouble().toBigDecimal()
                            else BigDecimal.ZERO

                        charts.add(CoinGeckoMarketChartsResponse(rate, volume, timestamp))
                    }

                } catch (e: Exception){
                    //ignore
                }
            }

            return charts
        }
    }
}

data class CoinGeckoCoinPriceResponse(
    val coinId: String,
    val rate: BigDecimal,
    val rateDiff24h: BigDecimal) {

    companion object {

        fun parseData(jsonValue: JsonValue, currencyCode: String, coinIds: List<String>): List<CoinGeckoCoinPriceResponse> {

            val coinGeckoPriceResponses = mutableListOf<CoinGeckoCoinPriceResponse>()

            coinIds.forEach { coinId ->
                try {

                    jsonValue.asObject().get(coinId)?.let { coinData ->

                        if (!coinData.isNull) {
                            val rate = coinData.asObject().get(currencyCode.toLowerCase())?.let {
                                if (it.isNull) BigDecimal.ZERO
                                else it.asDouble().toBigDecimal()
                            } ?: BigDecimal.ZERO

                            val rateDiff24h = coinData.asObject().get("${currencyCode.toLowerCase()}_24h_change")?.let {
                                if (it.isNull) BigDecimal.ZERO
                                else it.asDouble().toBigDecimal()
                            } ?: BigDecimal.ZERO

                            coinGeckoPriceResponses.add(CoinGeckoCoinPriceResponse(coinId, rate, rateDiff24h))
                        }
                    }

                } catch (e: Exception) {
                    //ignore
                }
            }

            return coinGeckoPriceResponses
        }
    }
}