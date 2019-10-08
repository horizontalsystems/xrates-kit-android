package io.horizontalsystems.xrateskit.storage

import java.math.BigDecimal

data class RateInfo(val coin: String, val currency: String, val value: BigDecimal, val timestamp: Long)
