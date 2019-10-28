package io.horizontalsystems.xrateskit.storage

import androidx.room.*
import io.horizontalsystems.xrateskit.entities.MarketInfoEntity

@Dao
interface MarketInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(all: List<MarketInfoEntity>)

    @Delete
    fun delete(stats: MarketInfoEntity)

    @Query("SELECT * FROM MarketInfoEntity WHERE coin = :coin AND currency = :currency ORDER BY timestamp")
    fun getMarketInfo(coin: String, currency: String): MarketInfoEntity?

    @Query("SELECT * FROM MarketInfoEntity WHERE coin IN(:coins) AND currency = :currency ORDER BY timestamp DESC")
    fun getOldList(coins: List<String>, currency: String): List<MarketInfoEntity>
}
