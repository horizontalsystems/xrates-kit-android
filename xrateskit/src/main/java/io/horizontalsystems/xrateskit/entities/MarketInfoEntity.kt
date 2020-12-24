package io.horizontalsystems.xrateskit.entities

import androidx.room.Entity
import java.math.BigDecimal

@Entity(primaryKeys = ["coinCode", "currencyCode"])
class MarketInfoEntity(
        var coinCode: String,
        val currencyCode: String,
        val rate: BigDecimal,
        val rateOpenDay: BigDecimal,
        val rateDiff: BigDecimal,
        val volume: BigDecimal,
        val marketCap: BigDecimal,
        val supply: BigDecimal,
        val timestamp: Long,
        val liquidity: BigDecimal = BigDecimal.ZERO,
        val rateDiff1h: BigDecimal = BigDecimal.ZERO,
        val rateDiff24h: BigDecimal = BigDecimal.ZERO,
        val rateDiff7d: BigDecimal = BigDecimal.ZERO,
        val rateDiff30d: BigDecimal = BigDecimal.ZERO,
        val rateDiff1y: BigDecimal = BigDecimal.ZERO
)
