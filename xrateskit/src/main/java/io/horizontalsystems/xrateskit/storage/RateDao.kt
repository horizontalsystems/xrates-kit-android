package io.horizontalsystems.xrateskit.storage

import androidx.room.*

@Dao
interface RateDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(rate: Rate)

    @Delete
    fun delete(rate: Rate)

    @Query("SELECT * FROM Rate WHERE coin = :coin AND currency = :currency LIMIT 1")
    fun getRate(coin: String, currency: String): Rate?
}
