package io.horizontalsystems.xrateskit.entities

import androidx.room.Entity
import io.horizontalsystems.coinkit.models.CoinType
import java.math.BigDecimal

@Entity(primaryKeys = ["coinType", "currency", "timestamp"])
class HistoricalRate(
        val coinType: CoinType,
        val currency: String,
        val value: BigDecimal,
        val timestamp: Long
)
