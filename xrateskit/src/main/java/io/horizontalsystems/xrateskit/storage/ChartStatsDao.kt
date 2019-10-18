package io.horizontalsystems.xrateskit.storage

import androidx.room.*
import io.horizontalsystems.xrateskit.entities.ChartStats
import io.horizontalsystems.xrateskit.entities.ChartType

@Dao
interface ChartStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(stats: List<ChartStats>)

    @Delete
    fun delete(stats: ChartStats)

    @Query("SELECT * FROM ChartStats WHERE coin = :coin AND currency = :currency AND type = :type ORDER BY timestamp LIMIT 1")
    fun getLast(coin: String, currency: String, type: ChartType): ChartStats?

    @Query("SELECT * FROM ChartStats WHERE coin = :coin AND currency = :currency AND type = :type ORDER BY timestamp")
    fun getList(coin: String, currency: String, type: ChartType): List<ChartStats>

    @Query("SELECT * FROM ChartStats WHERE type IN(:types) AND coin IN(:coins) AND currency = :currency ORDER BY timestamp DESC")
    fun getOldStats(types: List<ChartType>, coins: List<String>, currency: String): List<ChartStats>

}
