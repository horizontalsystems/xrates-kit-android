package io.horizontalsystems.xrateskit.storage

import androidx.room.TypeConverter
import io.horizontalsystems.xrateskit.entities.ChartType
import java.math.BigDecimal

class DatabaseConverters {

    // BigDecimal

    @TypeConverter
    fun fromString(value: String?): BigDecimal? {
        return value?.let { BigDecimal(it) }
    }

    @TypeConverter
    fun toString(bigDecimal: BigDecimal?): String? {
        return bigDecimal?.toPlainString()
    }

    // ChartType

    @TypeConverter
    fun toString(chartType: ChartType?): String? {
        return chartType?.name
    }

    @TypeConverter
    fun toChartType(string: String?): ChartType? {
        return ChartType.fromString(string)
    }

}
