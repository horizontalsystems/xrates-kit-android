package io.horizontalsystems.xrateskit.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eclipsesource.json.Json
import io.horizontalsystems.coinkit.models.CoinType
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

@Entity
class ResourceInfo(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val resourceType: ResourceType,
    val versionId: String,
    val updatedAt: Long = Date().time / 1000,
)

enum class ResourceType{
    COIN_INFO,
    PROVIDER_COINS,
    PROVIDER_COINS_PRIORITY,
    AUDIT_INFO_TIMESTAMP
}


data class CoinInfoResource(
    val coinInfos: List<CoinInfoEntity>,
    val categories: List<CoinCategory>,
    val coinsCategories: List<CoinCategoriesEntity>,
    val funds: List<CoinFund>,
    val coinFunds: List<CoinFundsEntity>,
    val fundCategories: List<CoinFundCategory>,
    val links: List<CoinLinksEntity>,
    val exchangeInfos: List<ExchangeInfoEntity>,
    val coinTreasuries: List<CoinTreasuryEntity>,
    val treasuryCompanies: List<TreasuryCompany>,
    val securityParameters: List<SecurityParameter>
){

    companion object{
        fun parseFile(quickParse: Boolean, inputStream: InputStream) : CoinInfoResource{
            val jsonObject = Json.parse(InputStreamReader(inputStream))
            val coinInfos = mutableListOf<CoinInfoEntity>()
            val categories = mutableListOf<CoinCategory>()
            val coinCategories = mutableListOf<CoinCategoriesEntity>()
            val funds = mutableListOf<CoinFund>()
            val coinFunds = mutableListOf<CoinFundsEntity>()
            val fundCategories = mutableListOf<CoinFundCategory>()
            val coinLinks = mutableListOf<CoinLinksEntity>()
            val exchangeInfos = mutableListOf<ExchangeInfoEntity>()
            val coinTreasuries = mutableListOf<CoinTreasuryEntity>()
            val treasuryCompanies = mutableListOf<TreasuryCompany>()
            val securityParameters = mutableListOf<SecurityParameter>()

            if(!quickParse) {
                jsonObject.asObject().get("exchanges").asArray().forEach { exchangeInfo ->
                    val id = exchangeInfo.asObject().get("id").asString()
                    val name = exchangeInfo.asObject().get("name").asString()
                    val imageUrl = exchangeInfo.asObject().get("image_url").asString()

                    exchangeInfos.add(ExchangeInfoEntity(id, name, imageUrl))
                }

                jsonObject.asObject().get("categories").asArray().forEach { category ->
                    val id = category.asObject().get("id").asString()
                    val name = category.asObject().get("name").asString()

                    categories.add(CoinCategory(id, name))
                }

                jsonObject.asObject().get("fund_categories")?.let { fundCategoriesJson ->
                    fundCategoriesJson.asArray().forEach { fund ->
                        val id = fund.asObject().get("id").asString()
                        val name = fund.asObject().get("name").asString()
                        val order = fund.asObject().get("order").asInt()

                        fundCategories.add(CoinFundCategory(id, name, order))
                    }
                }

                jsonObject.asObject().get("treasuries")?.let { coinTreasuriesJson ->
                    coinTreasuriesJson.asArray().forEach { treasury ->
                        val companyData = treasury.asObject().get("company").asObject()
                        val company = TreasuryCompany(
                            companyData.get("id").asString(),
                            companyData.get("name").asString(),
                            companyData.get("select").asString(),
                            companyData.get("country").asString()
                        )
                        treasury.asObject().get("data").asArray().forEach { treasuryData ->
                            val coinId = treasuryData.asObject().names()[0]
                            val coinType = CoinType.fromString(coinId)
                            val amount = treasuryData.asObject()[coinId].asObject().get("amount").asDouble().toBigDecimal()
                            coinTreasuries.add(CoinTreasuryEntity(coinType, company.id, amount))
                        }

                        treasuryCompanies.add(company)
                    }
                }

                jsonObject.asObject().get("funds")?.let { fundsJson ->
                    fundsJson.asArray().forEach { fund ->
                        val id = fund.asObject().get("id").asString()
                        val name = fund.asObject().get("name").asString()

                        val url = fund.asObject().get("url")?.let {
                            if (!it.isNull) it.asString()
                            else ""
                        } ?: ""

                        val categoryId = fund.asObject().get("category")?.let {
                            if (!it.isNull) it.asString()
                            else ""
                        } ?: ""

                        funds.add(CoinFund(id, name, url, categoryId))
                    }
                }

                jsonObject.asObject().get("coins").asArray().forEach { coinInfo ->

                    var coinId = coinInfo.asObject().get("id").asString()
                    if (coinId.contains("erc20|") || coinId.contains("bep20|"))
                        coinId = coinId.toLowerCase()
                    val coinType = CoinType.fromString(coinId)

                    val code = coinInfo.asObject().get("code").asString()
                    val name = coinInfo.asObject().get("name").asString()
                    val rating = if (coinInfo.asObject().get("rating") != null) {
                        if (!coinInfo.asObject().get("rating").isNull)
                            coinInfo.asObject().get("rating").asString()
                        else ""
                    } else ""
                    val description = if (coinInfo.asObject().get("description") != null) {
                        if (!coinInfo.asObject().get("description").isNull)
                            coinInfo.asObject().get("description").asString()
                        else ""
                    } else ""

                    coinInfos.add(CoinInfoEntity(coinType, code, name, rating, description))

                    coinInfo.asObject().get("security")?.let {
                        it.asObject().let { security ->

                            securityParameters.add(SecurityParameter(
                                coinType,
                                Level.valueOf(security.get("privacy").asString().toUpperCase()),
                                security.get("decentralized").asBoolean(),
                                security.get("confiscation_resistance").asBoolean(),
                                security.get("censorship_resistance").asBoolean()
                            ))
                        }
                    }

                    coinInfo.asObject().get("categories")?.let {
                        it.asArray().forEach { categoryId ->
                            coinCategories.add(CoinCategoriesEntity(coinType, categoryId.asString()))
                        }
                    }
                    coinInfo.asObject().get("funds")?.let {
                        it.asArray().forEach { fundId ->
                            coinFunds.add(CoinFundsEntity(coinType, fundId.asString()))
                        }
                    }

                    coinInfo.asObject().get("links")?.let { link ->
                        LinkType.values().forEach { linkType ->
                            link.asObject().get(linkType.name.toLowerCase())?.let {
                                if (!it.isNull)
                                    if (!it.asString().isNullOrEmpty()) {
                                        coinLinks.add(CoinLinksEntity(coinType, linkType, it.asString()))
                                    }
                            }
                        }
                    }
                }
            }

            return CoinInfoResource(coinInfos, categories, coinCategories, funds, coinFunds, fundCategories, coinLinks, exchangeInfos, coinTreasuries, treasuryCompanies, securityParameters)
        }
    }
}

data class ProviderCoinsResource(
    val providerCoins: List<ProviderCoinEntity>){

    companion object{
        fun parseFile(quickParse: Boolean, inputStream: InputStream) : ProviderCoinsResource{
            val jsonObject = Json.parse(InputStreamReader(inputStream))
            val providerCoins = mutableListOf<ProviderCoinEntity>()

            if(!quickParse) {
                jsonObject.asObject().get("coins").asArray().forEach { coinInfo ->
                    var coinId = coinInfo.asObject().get("id").asString()
                    if (coinId.contains("erc20|") || coinId.contains("bep20|"))
                        coinId = coinId.toLowerCase()

                    val code = coinInfo.asObject().get("code").asString()
                    val name = coinInfo.asObject().get("name").asString()
                    var cryptoCompareId: String? = null
                    var coinGeckoId: String? = null

                    if (coinInfo.asObject().get("external_id") != null) {
                        val ids = coinInfo.asObject().get("external_id").asObject()
                        if (ids.get("coingecko") != null) {
                            if (!ids.get("coingecko").isNull)
                                coinGeckoId = ids.get("coingecko").asString().toLowerCase()
                        }
                        if (ids.get("cryptocompare") != null) {
                            if (!ids.get("cryptocompare").isNull)
                                cryptoCompareId = ids.get("cryptocompare").asString().toLowerCase()
                        }
                    }

                    providerCoins.add(
                        ProviderCoinEntity(
                            CoinType.fromString(coinId),
                            code,
                            name,
                            coinGeckoId,
                            cryptoCompareId
                        )
                    )
                }
            }

            return ProviderCoinsResource(providerCoins)
        }
    }
}

