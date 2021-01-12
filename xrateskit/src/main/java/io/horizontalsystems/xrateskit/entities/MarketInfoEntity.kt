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
        val supply: BigDecimal,
        val timestamp: Long,
        val rateDiffPeriod: BigDecimal = BigDecimal.ZERO,
        val marketCap: BigDecimal? = null,
        val liquidity: BigDecimal? = null,
)
