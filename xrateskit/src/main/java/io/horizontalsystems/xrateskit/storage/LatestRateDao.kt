package io.horizontalsystems.xrateskit.storage

import androidx.room.*
import io.horizontalsystems.xrateskit.entities.LatestRate

@Dao
interface LatestRateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(rate: LatestRate)

    @Delete
    fun delete(rate: LatestRate)

    @Query("SELECT * FROM LatestRate WHERE coin = :coin AND currency = :currency LIMIT 1")
    fun getRate(coin: String, currency: String): LatestRate?
}
