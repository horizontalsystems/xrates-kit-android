package io.horizontalsystems.xrateskit.entities

import java.math.BigDecimal

data class TokenHolder(
    val address: String,
    val share: BigDecimal
)