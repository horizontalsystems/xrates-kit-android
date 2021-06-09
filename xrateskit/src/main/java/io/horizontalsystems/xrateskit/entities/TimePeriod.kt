package io.horizontalsystems.xrateskit.entities

enum class TimePeriod(val id: Int, val seconds: Long, val title: String, val interval: Int = 0) {
    ALL(0, 0, "All", 0),
    HOUR_1(1, 3600, "1h", 0),
    DAY_START(2, 0, "DayStart", 30),
    HOUR_24(3, 86400, "24h", 30),
    DAY_7(4, 604800,"7d", 4),
    DAY_14(6, 604800,"14d", 8),
    DAY_30(5, 2592000, "30d", 12),
    DAY_200(5, 2592000, "200d", 3),
    YEAR_1(6, 31104000, "1y", 7);

    fun getChartType(): ChartType{
        return when(this){
            DAY_7 -> ChartType.WEEKLY
            DAY_14 -> ChartType.WEEKLY2
            DAY_30 -> ChartType.MONTHLY
            DAY_200 -> ChartType.MONTHLY6
            else -> ChartType.DAILY
        }
    }
}