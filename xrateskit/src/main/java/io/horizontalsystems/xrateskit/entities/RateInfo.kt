package io.horizontalsystems.xrateskit.entities

import java.math.BigDecimal

data class RateInfo(val coin: String, val currency: String, val value: BigDecimal, val timestamp: Long)
