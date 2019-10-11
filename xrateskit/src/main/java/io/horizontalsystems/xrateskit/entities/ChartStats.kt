package io.horizontalsystems.xrateskit.entities

import androidx.room.Entity
import java.math.BigDecimal

@Entity(primaryKeys = ["type", "coin", "currency"])
class ChartStats(
        val type: ChartType,
        val coin: String,
        val currency: String,
        val value: BigDecimal,
        val timestamp: Long
)
