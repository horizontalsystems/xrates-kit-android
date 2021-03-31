package io.horizontalsystems.xrateskit.entities

import com.eclipsesource.json.JsonValue
import java.lang.Exception
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
                    val linksJsonObject = it.asObject()

                    linksJsonObject.get("homepage")?.let {
                        val homepage = it.asArray().firstOrNull()?.asString()
                        if (!homepage.isNullOrBlank()) {
                            links[LinkType.WEBSITE] = homepage
                        }
                    }

                    val twitterScreenName = linksJsonObject.getString("twitter_screen_name",null)
                    if (!twitterScreenName.isNullOrBlank()) {
                        links[LinkType.TWITTER] = "https://twitter.com/${twitterScreenName}"
                    }

                    val telegramChannelId = linksJsonObject.getString("telegram_channel_identifier", null)
                    if (!telegramChannelId.isNullOrBlank()) {
                        links[LinkType.TELEGRAM] = "https://t.me/${telegramChannelId}"
                    }

                    val subredditUrl = linksJsonObject.getString("subreddit_url", null)
                    if (!subredditUrl.isNullOrBlank()) {
                        links[LinkType.REDDIT] = subredditUrl
                    }

                    linksJsonObject.get("repos_url")?.asObject()?.get("github")?.let { github ->
                        val githubUrl = github.asArray().firstOrNull()?.asString()
                        if (!githubUrl.isNullOrBlank()) {
                            links[LinkType.GITHUB] = githubUrl
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
)

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
