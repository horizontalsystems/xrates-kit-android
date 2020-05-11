package io.horizontalsystems.xrateskit.storage

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.horizontalsystems.xrateskit.entities.ChartPointEntity
import io.horizontalsystems.xrateskit.entities.HistoricalRate
import io.horizontalsystems.xrateskit.entities.MarketInfoEntity
import io.horizontalsystems.xrateskit.entities.TopMarketCoin

@androidx.room.Database(version = 3, exportSchema = false, entities = [
    HistoricalRate::class,
    ChartPointEntity::class,
    MarketInfoEntity::class,
    TopMarketCoin::class
])

@TypeConverters(DatabaseConverters::class)

abstract class Database : RoomDatabase() {
    abstract val historicalRateDao: HistoricalRateDao
    abstract val chartPointDao: ChartStatsDao
    abstract val marketInfoDao: MarketInfoDao
    abstract val topMarketCoinDao: TopMarketCoinDao

    companion object {
        fun create(context: Context): Database {
            return Room.databaseBuilder(context, Database::class.java, "x-rate-database")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
        }
    }
}
