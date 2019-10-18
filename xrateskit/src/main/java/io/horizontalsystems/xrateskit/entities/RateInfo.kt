package io.horizontalsystems.xrateskit.entities

import java.math.BigDecimal
import java.util.*

class RateInfo(val value: BigDecimal, val timestamp: Long, private val expirationIntervalSeconds: Long) {

    fun isExpired(): Boolean {
        return timestamp < Date().time / 1000 - expirationIntervalSeconds
    }
}
