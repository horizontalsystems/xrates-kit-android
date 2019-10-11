package io.horizontalsystems.xrateskit.entities

import androidx.room.Entity
import java.math.BigDecimal

@Entity(primaryKeys = ["coin", "currency"])
class LatestRate(
        val coin: String,
        val currency: String,
        val value: BigDecimal,
        val timestamp: Long
)
