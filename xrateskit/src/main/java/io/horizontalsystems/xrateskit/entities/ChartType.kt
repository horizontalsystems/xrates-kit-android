package io.horizontalsystems.xrateskit.entities

enum class ChartType(val interval: Int, val points: Int, val resource: String) {
    DAILY(30, 48, "histominute"),
    WEEKLY(3, 56, "histohour"),
    MONTHLY(12, 60, "histohour"),
    MONTHLY6(3, 60, "histoday"),
    MONTHLY12(7, 52, "histoday");

    companion object {
        private val map = values().associateBy(ChartType::name)

        fun fromString(type: String?): ChartType? = map[type]
    }
}
