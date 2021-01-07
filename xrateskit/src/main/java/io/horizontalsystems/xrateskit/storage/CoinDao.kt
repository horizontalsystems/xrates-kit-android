package io.horizontalsystems.xrateskit.storage

import androidx.room.*
import io.horizontalsystems.xrateskit.entities.CoinEntity

@Dao
interface CoinDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(all: List<CoinEntity>)

    @Query("SELECT * FROM CoinEntity WHERE code IN(:coinCodes)")
    fun getCoinsByCodes(coinCodes: List<String>): List<CoinEntity>
}
