package io.horizontalsystems.xrateskit.storage

import androidx.room.*
import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.entities.MarketInfoEntity

@Dao
interface MarketInfoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(all: List<MarketInfoEntity>)

    @Delete
    fun delete(stats: MarketInfoEntity)

    @Query("SELECT * FROM MarketInfoEntity WHERE coinType = :coinType AND currencyCode = :currency ORDER BY timestamp")
    fun getMarketInfo(coinType: CoinType, currency: String): MarketInfoEntity?

    @Query("SELECT * FROM MarketInfoEntity WHERE coinType IN(:coinTypes) AND currencyCode = :currency ORDER BY timestamp DESC")
    fun getOldList(coinTypes: List<CoinType>, currency: String): List<MarketInfoEntity>
}
