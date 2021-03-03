package io.horizontalsystems.xrateskit.storage

import androidx.room.*
import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.entities.*

@Dao
interface CoinInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCoinInfo(all: List<CoinInfoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCoinCategories(all: List<CoinCategoriesEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCoinLinks(all: List<CoinLinksEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCoinCategory(all: List<CoinCategory>)

    @Query("DELETE FROM CoinCategory")
    fun deleteAllCoinCategories()

    @Query("DELETE FROM CoinCategoriesEntity")
    fun deleteAllCoinsCategories()

    @Query("DELETE FROM CoinLinksEntity")
    fun deleteAllCoinLinks()

    @Query("SELECT * FROM CoinCategory WHERE  id IN (SELECT categoryId FROM CoinCategoriesEntity WHERE coinType =:coinType)")
    fun getCoinCategories(coinType: CoinType): List<CoinCategory>

    @Query("SELECT * FROM CoinInfoEntity WHERE coinType IN (SELECT coinType FROM CoinCategoriesEntity WHERE categoryId =:categoryId)")
    fun getCoinInfoByCategory(categoryId: String): List<CoinInfoEntity>

    @Query("SELECT * FROM CoinLinksEntity WHERE coinType =:coinType")
    fun getCoinLinks(coinType: CoinType): List<CoinLinksEntity>

    @Query("SELECT * FROM CoinInfoEntity")
    fun getCoinInfos(): List<CoinInfoEntity>

    @Query("SELECT * FROM CoinInfoEntity WHERE coinType=:coinType LIMIT 1")
    fun getCoinInfo(coinType: CoinType): CoinInfoEntity?

    @Query("SELECT count(*) FROM CoinInfoEntity")
    fun getCoinInfoCount(): Int

}
