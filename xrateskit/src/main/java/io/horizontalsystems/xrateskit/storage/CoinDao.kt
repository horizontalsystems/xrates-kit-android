package io.horizontalsystems.xrateskit.storage

import androidx.room.*
import io.horizontalsystems.xrateskit.entities.CoinInfoEntity

@Dao
interface CoinDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(all: List<CoinInfoEntity>)

    @Query("SELECT * FROM CoinInfoEntity WHERE code IN(:coinCodes)")
    fun getCoinsByCodes(coinCodes: List<String>): List<CoinInfoEntity>

    @Query("SELECT count(*) FROM CoinInfoEntity")
    fun getRecordCount(): Int
}
