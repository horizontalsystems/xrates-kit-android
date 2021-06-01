package io.horizontalsystems.xrateskit.entities

import java.math.BigDecimal

class CoinMarket(
    val data: CoinData,
    val marketInfo: MarketInfo
)

class CoinMarketPoint(
    val timestamp: Long,
    val marketCap: BigDecimal,
    val volume24h: BigDecimal,
)

class CoinMarketDetails(
    val data: CoinData,
    val meta: CoinMeta,
    val currencyCode: String,

    val rate: BigDecimal,
    val rateHigh24h: BigDecimal,
    val rateLow24h: BigDecimal,

    val totalSupply: BigDecimal,
    val circulatingSupply: BigDecimal,

    val volume24h: BigDecimal,

    val marketCap: BigDecimal,
    val marketCapDiff24h: BigDecimal,
    val marketCapRank: Int?,

    val dilutedMarketCap: BigDecimal?,

    val rateDiffs: Map<TimePeriod, Map<String, BigDecimal>>,
    val tickers: List<MarketTicker>,

    var defiTvlInfo: DefiTvlInfo? = null
)

class MarketTicker(
    val base: String,
    val target: String,
    val marketName: String,
    val rate: BigDecimal,
    val volume: BigDecimal,
    val imageUrl: String? = null
)

class DefiTvlInfo(
    val tvl: BigDecimal,
    val tvlRank: Int,
    val marketCapTvlRatio: BigDecimal
)