package io.horizontalsystems.xrateskit.storage

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class Rate(
        @PrimaryKey
        val coin: String,
        val currency: String,
        val value: Double,
        val timestamp: Long
)
