package io.horizontalsystems.xrateskit.entities

import androidx.room.Entity

@Entity(primaryKeys = ["code"])
data class TopMarketCoin(
    val code: String,
    val name: String
)
