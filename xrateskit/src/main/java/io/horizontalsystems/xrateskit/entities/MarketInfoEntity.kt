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
        val rateDiffPeriod: BigDecimal = BigDecimal.ZERO,
)
