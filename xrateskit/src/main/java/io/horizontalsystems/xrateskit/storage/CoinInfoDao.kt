package io.horizontalsystems.xrateskit.storage

import androidx.room.*
import io.horizontalsystems.xrateskit.entities.CoinCategoriesEntity
import io.horizontalsystems.xrateskit.entities.CoinCategory
import io.horizontalsystems.xrateskit.entities.CoinInfoEntity

@Dao
interface CoinInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCoinInfo(all: List<CoinInfoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCoinCategories(all: List<CoinCategoriesEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCoinCategory(all: List<CoinCategory>)

    @Query("SELECT * FROM CoinCategory WHERE id in (select categoryId from CoinCategoriesEntity where coinId =:coinId)")
    fun getCoinCategories(coinId: String): List<CoinCategory>

    @Query("SELECT * FROM CoinInfoEntity WHERE id in (select coinId from CoinCategoriesEntity where categoryId =:categoryId)")
    fun getCoinInfoByCategory(categoryId: String): List<CoinInfoEntity>

    @Query("SELECT count(*) FROM CoinInfoEntity")
    fun getCoinInfoCount(): Int
}
