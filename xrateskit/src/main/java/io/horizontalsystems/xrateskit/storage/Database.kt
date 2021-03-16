package io.horizontalsystems.xrateskit.storage

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import io.horizontalsystems.xrateskit.entities.*

@androidx.room.Database(version = 12, exportSchema = false, entities = [
    HistoricalRate::class,
    ChartPointEntity::class,
    GlobalCoinMarket::class,
    ProviderCoinEntity::class,
    CoinInfoEntity::class,
    CoinCategory::class,
    CoinCategoriesEntity::class,
    ResourceInfo::class,
    CoinLinksEntity::class,
    LatestRateEntity::class
])

@TypeConverters(DatabaseConverters::class)

abstract class Database : RoomDatabase() {
    abstract val providerCoinsDao: ProviderCoinsDao
    abstract val historicalRateDao: HistoricalRateDao
    abstract val chartPointDao: ChartStatsDao
    abstract val latestRatesDao: LatestRatesDao
    abstract val globalMarketInfoDao: GlobalMarketInfoDao
    abstract val coinInfoDao: CoinInfoDao
    abstract val resourceInfoDao: ResourceInfoDao

    companion object {
        fun create(context: Context): Database {
            return Room.databaseBuilder(context, Database::class.java, "x-rate-database")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
        }
    }
}
