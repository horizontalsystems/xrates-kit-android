package io.horizontalsystems.xrateskit.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.entities.*

@Dao
interface CoinInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertSecurityParameters(all: List<SecurityParameter>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCoinTreasuries(all: List<CoinTreasuryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertTreasuryCompanies(all: List<TreasuryCompany>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCoinInfo(all: List<CoinInfoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertExchangeInfo(all: List<ExchangeInfoEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCoinCategories(all: List<CoinCategoriesEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCoinFunds(all: List<CoinFundsEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCoinLinks(all: List<CoinLinksEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCoinCategory(all: List<CoinCategory>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCoinFund(all: List<CoinFund>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertCoinFundCategory(all: List<CoinFundCategory>)

    @Query("DELETE FROM SecurityParameter")
    fun deleteAllSecurityParameters()

    @Query("DELETE FROM CoinTreasuryEntity")
    fun deleteAllCoinTreasuries()

    @Query("DELETE FROM TreasuryCompany")
    fun deleteAllTreasuryCompanies()

    @Query("DELETE FROM CoinCategory")
    fun deleteAllCoinCategories()

    @Query("DELETE FROM ExchangeInfoEntity")
    fun deleteAllExchangeInfo()

    @Query("DELETE FROM CoinCategoriesEntity")
    fun deleteAllCoinsCategories()

    @Query("DELETE FROM CoinFundCategory")
    fun deleteAllCoinFundCategories()

    @Query("DELETE FROM CoinFundsEntity")
    fun deleteAllCoinsFunds()

    @Query("DELETE FROM CoinFund")
    fun deleteAllCoinFunds()

    @Query("DELETE FROM CoinLinksEntity")
    fun deleteAllCoinLinks()

    @Query("SELECT * FROM SecurityParameter WHERE coinType=:coinType")
    fun getSecurityParameter(coinType: CoinType): SecurityParameter?

    @Query("SELECT * FROM CoinCategory WHERE id IN (SELECT categoryId FROM CoinCategoriesEntity WHERE coinType =:coinType)")
    fun getCoinCategories(coinType: CoinType): List<CoinCategory>

    @Query("SELECT * FROM ExchangeInfoEntity WHERE id=:exchangeId")
    fun getExchangeInfo(exchangeId: String): ExchangeInfoEntity?

    @Query("SELECT * FROM CoinFund WHERE id IN (SELECT fundId FROM CoinFundsEntity WHERE coinType =:coinType)")
    fun getCoinFunds(coinType: CoinType): List<CoinFund>

    @Query("SELECT * FROM CoinFundCategory WHERE id IN(:categoryIds) ORDER BY `order`")
    fun getCoinFundCategories(categoryIds: List<String>): List<CoinFundCategory>

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

    @Query("SELECT DISTINCT coinType FROM CoinCategoriesEntity")
    fun getCategorizedCoinTypes(): List<CoinType>

    @Query("SELECT * FROM CoinTreasuryEntity where coinType=:coinType")
    fun getCoinTreasuries(coinType: CoinType): List<CoinTreasuryEntity>

    @Query("SELECT * FROM TreasuryCompany where id IN(:companyIds)")
    fun getTreasuryCompanies(companyIds: List<String>): List<TreasuryCompany>
}
