package io.horizontalsystems.xrateskit.coininfo

import android.content.Context
import com.eclipsesource.json.Json
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.CoinCategoriesEntity
import io.horizontalsystems.xrateskit.entities.CoinCategory
import io.horizontalsystems.xrateskit.entities.CoinInfo
import io.horizontalsystems.xrateskit.entities.CoinInfoEntity
import java.io.InputStreamReader


class CoinInfoManager(
    private val storage: IStorage,
    private val context: Context
) {

    private val coinInfoFileName = "coins.json"

    fun loadCoinInfo(){
        if(storage.getCoinInfoCount() < 1 ){
            parseCoinsInfoFile()
        }
    }

    private fun parseCoinsInfoFile() {
        val inputStream = context.assets.open(coinInfoFileName)
        val jsonObject = Json.parse(InputStreamReader(inputStream))
        val coinInfos = mutableListOf<CoinInfoEntity>()
        val categories = mutableListOf<CoinCategory>()
        val coinCategories = mutableListOf<CoinCategoriesEntity>()

        jsonObject.asObject().get("categories").asArray().forEach { category ->
            val id = category.asObject().get("id").asString()
            val name = category.asObject().get("name").asString()

            categories.add(CoinCategory(id, name))
        }

        jsonObject.asObject().get("coins").asArray().forEach { coinInfo ->
            val coinId = coinInfo.asObject().get("id").asString()
            val code = coinInfo.asObject().get("code").asString()
            val name = coinInfo.asObject().get("name").asString()
            val description = coinInfo.asObject().get("description").asString()

            coinInfos.add(CoinInfoEntity(coinId, code, name, description))

            coinInfo.asObject().get("categories")?.let {
                it.asArray().forEach {  categoryId ->
                    coinCategories.add(CoinCategoriesEntity(coinId, categoryId.asString()))
                }
            }
        }

        storage.saveCoinInfo(coinInfos)
        storage.saveCoinCategories(coinCategories)
        storage.saveCoinCategory(categories)
    }

    fun getCoinCodesByCategory(categoryId: String): List<String> {
        val coinInfoEntity = storage.getCoinInfo(categoryId)
        return coinInfoEntity.map { it.code }
    }

}