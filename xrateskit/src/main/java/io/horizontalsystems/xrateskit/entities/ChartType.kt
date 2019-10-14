package io.horizontalsystems.xrateskit.entities

import java.util.*

enum class ChartType(val interval: Long, val points: Int, val resource: String) {
    DAILY(30, 49, "histominute"),   // minutes
    WEEKLY(3, 57, "histohour"),     // hourly
    MONTHLY(12, 61, "histohour"),   // hourly
    MONTHLY6(3, 61, "histoday"),    // daily
    MONTHLY12(7, 53, "histoday");   // daily

    val minutes: Long
        get() = when (this) {
            DAILY -> interval
            WEEKLY -> interval * 60
            MONTHLY -> interval * 60
            MONTHLY6 -> interval * 24 * 60
            MONTHLY12 -> interval * 24 * 60
        }

    fun isExpired(timestamp: Long): Boolean {
        val currentTimeSecond = Date().time / 1000
        val intervalInSeconds = minutes * 60

        return timestamp < currentTimeSecond - intervalInSeconds
    }

    companion object {
        private val map = values().associateBy(ChartType::name)

        fun fromString(type: String?): ChartType? = map[type]
    }
}
