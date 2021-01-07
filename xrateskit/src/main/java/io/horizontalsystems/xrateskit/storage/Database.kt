package io.horizontalsystems.xrateskit.storage

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.horizontalsystems.xrateskit.entities.*

@androidx.room.Database(version = 4, exportSchema = false, entities = [
    CoinEntity::class,
    HistoricalRate::class,
    ChartPointEntity::class,
    MarketInfoEntity::class,
    GlobalCoinMarket::class
])

@TypeConverters(DatabaseConverters::class)

abstract class Database : RoomDatabase() {
    abstract val coinDao: CoinDao
    abstract val historicalRateDao: HistoricalRateDao
    abstract val chartPointDao: ChartStatsDao
    abstract val marketInfoDao: MarketInfoDao
    abstract val globalMarketInfoDao: GlobalMarketInfoDao

    companion object {
        fun create(context: Context): Database {
            return Room.databaseBuilder(context, Database::class.java, "x-rate-database")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
        }
    }
}
