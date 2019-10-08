package io.horizontalsystems.xrateskit.storage

import androidx.room.*

@Dao
interface HistoricalRateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(rate: HistoricalRate)

    @Delete
    fun delete(rate: HistoricalRate)

    @Query("SELECT * FROM HistoricalRate WHERE coin = :coin AND currency = :currency AND timestamp = :timestamp LIMIT 1")
    fun getRate(coin: String, currency: String, timestamp: Long): HistoricalRate?
}
