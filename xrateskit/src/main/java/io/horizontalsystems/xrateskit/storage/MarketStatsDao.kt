package io.horizontalsystems.xrateskit.storage

import androidx.room.*
import io.horizontalsystems.xrateskit.entities.ChartType
import io.horizontalsystems.xrateskit.entities.MarketStats

@Dao
interface MarketStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(stats: MarketStats)

    @Delete
    fun delete(stats: MarketStats)

    @Query("SELECT * FROM MarketStats WHERE coin = :coin AND currency = :currency ORDER BY timestamp")
    fun getMarketStats(coin: String, currency: String): MarketStats?
}
