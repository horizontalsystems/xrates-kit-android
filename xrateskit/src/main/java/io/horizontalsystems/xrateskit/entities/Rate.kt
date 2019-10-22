package io.horizontalsystems.xrateskit.entities

import java.math.BigDecimal
import java.util.*

class Rate(val value: BigDecimal, val timestamp: Long, private val expirationInterval: Long) {

    fun isExpired(): Boolean {
        return Date().time / 1000 - expirationInterval > timestamp
    }
}
