package io.horizontalsystems.xrateskit.storage

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.horizontalsystems.xrateskit.entities.ChartPointEntity
import io.horizontalsystems.xrateskit.entities.HistoricalRate
import io.horizontalsystems.xrateskit.entities.LatestRate
import io.horizontalsystems.xrateskit.entities.MarketStats

@androidx.room.Database(version = 1, exportSchema = false, entities = [
    LatestRate::class,
    HistoricalRate::class,
    ChartPointEntity::class,
    MarketStats::class
])

@TypeConverters(DatabaseConverters::class)

abstract class Database : RoomDatabase() {
    abstract val latestRateDao: LatestRateDao
    abstract val historicalRateDao: HistoricalRateDao
    abstract val chartStatsDao: ChartStatsDao
    abstract val marketStatsDao: MarketStatsDao

    companion object {
        fun create(context: Context): Database {
            return Room.databaseBuilder(context, Database::class.java, "x-rate-database")
                    .allowMainThreadQueries()
                    .build()
        }
    }
}
