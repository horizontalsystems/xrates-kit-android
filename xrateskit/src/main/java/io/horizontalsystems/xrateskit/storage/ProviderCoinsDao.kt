package io.horizontalsystems.xrateskit.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.horizontalsystems.xrateskit.entities.ProviderCoinEntity

@Dao
interface ProviderCoinInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(all: List<ProviderCoinEntity>)

    @Query("SELECT * FROM ProviderCoinEntity WHERE id=:coinId")
    fun get(providerId : Int, coinCodes: List<String>): List<ProviderCoinInfo>

    @Query("SELECT count(*) FROM ProviderCoinEntity WHERE providerId=:providerId")
    fun getRecordCount(providerId : Int): Int
}