package io.horizontalsystems.xrateskit.entities

import androidx.room.Entity
import java.math.BigDecimal


@Entity(primaryKeys = ["currencyCode"])
data class GlobalCoinMarket(
    val currencyCode: String,
    val volume24h: BigDecimal,
    val volume24hDiff24h: BigDecimal,
    val marketCap: BigDecimal,
    val marketCapDiff24h: BigDecimal,
    var btcDominance: BigDecimal = BigDecimal.ZERO,
    var btcDominanceDiff24h: BigDecimal = BigDecimal.ZERO,
    var defiMarketCap: BigDecimal = BigDecimal.ZERO,
    var defiMarketCapDiff24h: BigDecimal = BigDecimal.ZERO,
    var defiTvl: BigDecimal = BigDecimal.ZERO,
    var defiTvlDiff24h: BigDecimal = BigDecimal.ZERO
)
