package io.horizontalsystems.xrateskit.entities

import androidx.room.Entity
import java.math.BigDecimal

@Entity(primaryKeys = ["coin", "currency"])
class MarketInfoEntity(
        val coin: String,
        val currency: String,
        val rate: BigDecimal,
        val rateOpen24Hour: BigDecimal,
        val diff: BigDecimal,
        val volume: Double,
        val marketCap: Double,
        val supply: Double,
        val timestamp: Long
)
