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
)

data class CoinGeckoCoinInfo(
    val coinId: String,
    val coinCode: String,
    val title: String,
    val description: String = "",
    val links: Map<LinkType, String> = emptyMap(),
    val platforms: Map<CoinPlatformType, String> = emptyMap(),
    val tickers: List<CoinGeckoTickersResponse> = emptyList()
)

data class CoinGeckoTickersResponse(
    val base: String,
    val target: String,
    val marketName: String,
    val marketId: String,
    val rate: BigDecimal = BigDecimal.ZERO,
    val volume: BigDecimal = BigDecimal.ZERO)

data class CoinGeckoCoinMarketDetailsResponse(
    val coinInfo: CoinGeckoCoinInfo,
    val coinGeckoMarkets: CoinGeckoCoinMarkets,
    val rateDiffs: Map<TimePeriod, Map<String, BigDecimal>>
)
