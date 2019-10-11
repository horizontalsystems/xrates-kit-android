package io.horizontalsystems.xrateskit.entities

import androidx.room.Entity
import java.math.BigDecimal

@Entity(primaryKeys = ["coin", "currency", "timestamp"])
class HistoricalRate(
        val coin: String,
        val currency: String,
        val value: BigDecimal,
        val timestamp: Long
)
