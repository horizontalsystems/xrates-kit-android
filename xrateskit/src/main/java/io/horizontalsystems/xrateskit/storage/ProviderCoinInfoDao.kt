package io.horizontalsystems.xrateskit.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.horizontalsystems.xrateskit.entities.ProviderCoinInfo

@Dao
interface ProviderCoinInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(all: List<ProviderCoinInfo>)

    @Query("SELECT * FROM ProviderCoinInfo WHERE providerId=:providerId AND coinCode IN(:coinCodes)")
    fun getCoinsByCodes(providerId : Int, coinCodes: List<String>): List<ProviderCoinInfo>

    @Query("SELECT count(*) FROM ProviderCoinInfo WHERE providerId=:providerId")
    fun getRecordCount(providerId : Int): Int
}