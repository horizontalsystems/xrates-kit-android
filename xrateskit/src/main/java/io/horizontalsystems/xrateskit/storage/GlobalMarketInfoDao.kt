package io.horizontalsystems.xrateskit.storage

import androidx.room.*
import io.horizontalsystems.xrateskit.entities.GlobalCoinMarket

@Dao
interface GlobalMarketInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(globalCoinMarket: GlobalCoinMarket)

    @Delete
    fun delete(globalCoinMarket: GlobalCoinMarket)

    @Query("SELECT * FROM GlobalCoinMarket WHERE currencyCode = :currency")
    fun getGlobalMarketInfo(currency: String): GlobalCoinMarket?
}
