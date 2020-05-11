package io.horizontalsystems.xrateskit.storage

import androidx.room.*
import io.horizontalsystems.xrateskit.entities.TopMarketCoin

@Dao
interface TopMarketCoinDao {
    @Query("SELECT * FROM TopMarketCoin ORDER BY rowId")
    fun getAll(): List<TopMarketCoin>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(list: List<TopMarketCoin>)

    @Query("DELETE FROM TopMarketCoin")
    fun deleteAll()
}
