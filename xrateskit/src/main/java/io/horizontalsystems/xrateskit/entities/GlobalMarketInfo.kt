package io.horizontalsystems.xrateskit.entities


data class GlobalMarketInfo(
    val volume24h: Double,
    val volume24hPercentChange24h: Double,
    val marketCap: Double,
    val marketCapPercentChange24h: Double,
    var btcDominance: Double,
    var btcDominancePercentChange24h: Double = 0.0,
    var defiMarketCap: Double = 0.0,
    var defiMarketCapPercentChange24h: Double = 0.0,
    var defiTvl: Double = 0.0
)
