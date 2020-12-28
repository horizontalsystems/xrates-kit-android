package io.horizontalsystems.xrateskit.entities

enum class TimePeriod(val id: Int, val seconds: Long, val title: String) {
    ALL(0, 0, "All"),
    HOUR_1(1, 3600, "1h"),
    DAY_START(2, 0, "DayStart"),
    HOUR_24(3, 86400, "24h"),
    DAY_7(4, 604800,"7d"),
    DAY_30(5, 2592000, "30d"),
    YEAY_1(6, 31104000,"1y");
}