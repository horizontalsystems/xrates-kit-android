package io.horizontalsystems.xrateskit.entities

enum class ChartType(val interval: Long, val points: Int, val resource: String) {
    DAILY(5, 48, "histominute"),    // minutes
    WEEKLY(3, 56, "histohour"),     // hourly
    MONTHLY(12, 60, "histohour"),   // hourly
    MONTHLY6(3, 60, "histoday"),    // daily
    MONTHLY12(7, 52, "histoday");   // daily

    val seconds: Long
        get() = when (this) {
            DAILY -> interval
            WEEKLY -> interval * 60
            MONTHLY -> interval * 60
            MONTHLY6 -> interval * 24 * 60
            MONTHLY12 -> interval * 24 * 60
        } * 60

    companion object {
        private val map = values().associateBy(ChartType::name)

        fun fromString(type: String?): ChartType? = map[type]
    }
}
