package io.horizontalsystems.xrateskit.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.horizontalsystems.xrateskit.entities.ChartPointEntity
import io.horizontalsystems.xrateskit.entities.ChartType

@Dao
interface ChartStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(stats: List<ChartPointEntity>)

    @Query("DELETE FROM ChartPointEntity WHERE coin = :coin AND currency = :currency AND type = :chartType")
    fun delete(coin: String, currency: String, chartType: ChartType)

    @Query("SELECT * FROM ChartPointEntity WHERE coin = :coin AND currency = :currency AND type = :type ORDER BY timestamp DESC LIMIT 1")
    fun getLast(coin: String, currency: String, type: ChartType): ChartPointEntity?

    @Query("SELECT * FROM ChartPointEntity WHERE coin = :coin AND currency = :currency AND type = :type ORDER BY timestamp")
    fun getList(coin: String, currency: String, type: ChartType): List<ChartPointEntity>

    @Query("SELECT * FROM ChartPointEntity WHERE coin = :coin AND currency = :currency AND type = :type AND timestamp >= :fromTimestamp ORDER BY timestamp")
    fun getList(coin: String, currency: String, type: ChartType, fromTimestamp: Long): List<ChartPointEntity>

}
