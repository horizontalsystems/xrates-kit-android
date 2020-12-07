package io.horizontalsystems.xrateskit.entities

import androidx.room.Entity
import java.math.BigDecimal

@Entity(primaryKeys = ["coin", "currency"])
class MarketInfoEntity(
        var coin: String,
        val currency: String,
        val rate: BigDecimal,
        val rateOpenDay: BigDecimal,
        val diff: BigDecimal,
        val volume: Double,
        val marketCap: Double,
        val supply: Double,
        val timestamp: Long
)
