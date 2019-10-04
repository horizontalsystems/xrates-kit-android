package io.horizontalsystems.xrateskit.api

data class CryptoCompareResponse(
        val coin: String,
        val currency: String,
        val value: Double
)
