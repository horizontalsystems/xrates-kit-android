package io.horizontalsystems.xrateskit.entities

import java.math.BigDecimal

class CoinMarketDetails(
    val coin: Coin,
    val currencyCode: String,

    val rate: BigDecimal,
    val rateHigh24h: BigDecimal,
    val rateLow24h: BigDecimal,

    val totalSupply: BigDecimal,
    val circulatingSupply: BigDecimal,

    val volume24h: BigDecimal,

    val marketCap: BigDecimal,
    val marketCapDiff24h: BigDecimal,

    val coinInfo: CoinInfo,
    val rateDiffs: Map<TimePeriod, Map<String, BigDecimal> >
)

class CoinInfo(
    val description: String,
    val links: Map<String, String>)
