package io.horizontalsystems.xrateskit.entities

import androidx.room.Entity

@Entity(primaryKeys = ["providerId", "coinCode"])
data class ProviderCoinInfo(
    var providerId: Int,
    val coinCode: String,
    val providerCoinId: String
)
