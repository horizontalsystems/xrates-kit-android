package io.horizontalsystems.xrateskit.entities

import androidx.room.Entity

@Entity(primaryKeys = ["coin", "currency"])
class MarketStats(
        val coin: String,
        val currency: String,
        val volume: Double,
        val marketCap: Double,
        val supply: Double,
        val timestamp: Long
)
