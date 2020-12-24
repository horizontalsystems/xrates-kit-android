package io.horizontalsystems.xrateskit.storage

import androidx.room.*
import io.horizontalsystems.xrateskit.entities.GlobalMarketInfo
import io.horizontalsystems.xrateskit.entities.MarketInfoEntity

@Dao
interface GlobalMarketInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(globalMarketInfo: GlobalMarketInfo)

    @Delete
    fun delete(globalMarketInfo: GlobalMarketInfo)

    @Query("SELECT * FROM GlobalMarketInfo WHERE currencyCode = :currency")
    fun getGlobalMarketInfo(currency: String): GlobalMarketInfo?
}
