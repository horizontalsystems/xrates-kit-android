package io.horizontalsystems.xrateskit.entities

enum class ChartType(val interval: Long, val points: Int, val resource: String) {
    DAILY(30, 48, "histominute"),   // minutes
    WEEKLY(4, 48, "histohour"),     // hourly
    MONTHLY(12, 60, "histohour"),   // hourly
    MONTHLY3(2, 45, "histoday"),    // daily
    MONTHLY6(3, 60, "histoday"),    // daily
    MONTHLY12(7, 52, "histoday"),   // daily
    MONTHLY24(14, 52, "histoday");  // daily

    val expirationInterval: Long
        get() {
            val multiplier = when (resource) {
                "histominute" -> 60
                "histohour" -> 60 * 60
                "histoday" -> 24 * 60 * 60
                else -> 60
            }

            return interval * multiplier
        }

    val rangeInterval: Long
        get() = expirationInterval * points

    val seconds: Long
        get() = when (this) {
            DAILY -> interval
            WEEKLY -> interval * 60
            MONTHLY -> interval * 60
            MONTHLY3 -> interval * 24 * 60
            MONTHLY6 -> interval * 24 * 60
            MONTHLY12 -> interval * 24 * 60
            MONTHLY24 -> interval * 24 * 60
        } * 60

    companion object {
        private val map = values().associateBy(ChartType::name)

        fun fromString(type: String?): ChartType? = map[type]
    }
}
