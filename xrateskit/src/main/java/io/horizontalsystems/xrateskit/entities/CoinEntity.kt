package io.horizontalsystems.xrateskit.entities

import androidx.room.Entity
import java.math.BigDecimal

@Entity(primaryKeys = ["code", "title"])
class CoinEntity(
        var code: String,
        val title: String,
        val type: Int?,
        val contractAddress: String,
)
