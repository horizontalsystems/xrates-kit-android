package io.horizontalsystems.xrateskit.entities

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eclipsesource.json.Json
import io.horizontalsystems.coinkit.models.CoinType
import java.io.InputStreamReader

@Entity
class ResourceInfo(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val resourceType: ResourceType,
    val version: Int)

enum class ResourceType{
    COIN_INFO,
    PROVIDER_COINS,
}


data class CoinInfoResource(
    val version:Int,
    val coinInfos: List<CoinInfoEntity>,
    val categories: List<CoinCategory>,
    val coinCategories: List<CoinCategoriesEntity>,
    val links: List<CoinLinksEntity>){

    companion object{
        fun parseFile(context: Context, fileName: String) : CoinInfoResource{
            val inputStream = context.assets.open(fileName)
            val jsonObject = Json.parse(InputStreamReader(inputStream))
            val version = jsonObject.asObject().get("version").asInt()
            val coinInfos = mutableListOf<CoinInfoEntity>()
            val categories = mutableListOf<CoinCategory>()
            val coinCategories = mutableListOf<CoinCategoriesEntity>()
            val coinLinks = mutableListOf<CoinLinksEntity>()

            jsonObject.asObject().get("categories").asArray().forEach { category ->
                val id = category.asObject().get("id").asString()
                val name = category.asObject().get("name").asString()

                categories.add(CoinCategory(id, name))
            }

            jsonObject.asObject().get("coins").asArray().forEach { coinInfo ->

                var coinId = coinInfo.asObject().get("id").asString()
                if(coinId.contains("erc20|") || coinId.contains("bep20|"))
                    coinId = coinId.toLowerCase()
                val coinType = CoinType.fromString(coinId)

                val code = coinInfo.asObject().get("code").asString()
                val name = coinInfo.asObject().get("name").asString()
                val rating = if(coinInfo.asObject().get("rating") != null){
                    if(!coinInfo.asObject().get("rating").isNull)
                        coinInfo.asObject().get("rating").asString()
                    else ""
                } else ""
                val description = if(coinInfo.asObject().get("description") != null){
                    if(!coinInfo.asObject().get("description").isNull)
                        coinInfo.asObject().get("description").asString()
                    else ""
                } else ""

                coinInfos.add(CoinInfoEntity(coinType, code, name, rating, description))

                coinInfo.asObject().get("categories")?.let {
                    it.asArray().forEach {  categoryId ->
                        coinCategories.add(CoinCategoriesEntity(coinType, categoryId.asString()))
                    }
                }

                coinInfo.asObject().get("links")?.let { link ->
                    LinkType.values().forEach { linkType ->
                        link.asObject().get(linkType.name.toLowerCase())?.let {
                            if(!it.isNull)
                                if(!it.asString().isNullOrEmpty()){
                                    coinLinks.add(CoinLinksEntity(coinType, linkType, it.asString()))
                                }
                        }
                    }
                }
            }

            return CoinInfoResource(version, coinInfos, categories, coinCategories, coinLinks)
        }
    }
}

data class ProviderCoinsResource(
    val version:Int,
    val providerCoins: List<ProviderCoinEntity>){

    companion object{
        fun parseFile(context: Context, fileName: String) : ProviderCoinsResource{
            val inputStream = context.assets.open(fileName)
            val jsonObject = Json.parse(InputStreamReader(inputStream))
            val version = jsonObject.asObject().get("version").asInt()
            val providerCoins = mutableListOf<ProviderCoinEntity>()

            jsonObject.asObject().get("coins").asArray().forEach { coinInfo ->
                var coinId = coinInfo.asObject().get("id").asString()
                if(coinId.contains("erc20|") || coinId.contains("bep20|"))
                    coinId = coinId.toLowerCase()

                val code = coinInfo.asObject().get("code").asString()
                val name = coinInfo.asObject().get("name").asString()
                var cryptoCompareId: String? = null
                var coinGeckoId: String? = null

                if(coinInfo.asObject().get("external_id") != null){
                    val ids = coinInfo.asObject().get("external_id").asObject()
                    if(ids.get("coingecko") != null){
                        if(!ids.get("coingecko").isNull)
                            coinGeckoId = ids.get("coingecko").asString().toLowerCase()
                    }
                    if(ids.get("cryptocompare") != null){
                        if(!ids.get("cryptocompare").isNull)
                            cryptoCompareId = ids.get("cryptocompare").asString().toLowerCase()
                    }
                }

                providerCoins.add(ProviderCoinEntity(CoinType.fromString(coinId), code, name, coinGeckoId, cryptoCompareId))
            }

            return ProviderCoinsResource(version, providerCoins)
        }
    }
}

