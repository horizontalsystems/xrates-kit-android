package io.horizontalsystems.xrateskit.coins

import android.content.Context
import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Single

class CoinInfoManager(
    private val context: Context,
    private val storage: IStorage
) {

    private val coinInfoFileName = "coins.json"

    init {
        updateCoinInfo()
    }

    private fun updateCoinInfo() {
        val coinsResponse = CoinInfoResource.parseFile(context, coinInfoFileName)
        val resourceInfo = storage.getResourceInfo(ResourceType.COIN_INFO)

        val update = resourceInfo?.let {
            coinsResponse.version != it.version
        } ?: true

        if (update) {
            storage.deleteAllCoinCategories()
            storage.deleteAllCoinLinks()
            storage.deleteAllCoinsCategories()

            storage.saveCoinInfos(coinsResponse.coinInfos)
            storage.saveCoinCategories(coinsResponse.coinCategories)
            storage.saveCoinCategory(coinsResponse.categories)
            storage.saveCoinLinks(coinsResponse.links)
            storage.saveResourceInfo(ResourceInfo(ResourceType.COIN_INFO, coinsResponse.version))
        }
    }

    fun getCoinRating(coinType: CoinType): String? {
        return storage.getCoinInfo(coinType)?.rating
    }

    fun getCoinCategories(coinType: CoinType): List<CoinCategory> {
        return storage.getCoinInfo(coinType)?.let {
            storage.getCoinCategories(it.coinType)
        } ?: emptyList()
    }

    fun getCoinCodesByCategory(categoryId: String): List<CoinType> {
        val coinInfoEntity = storage.getCoinInfosByCategory(categoryId)
        return coinInfoEntity.map { it.coinType }
    }

    fun getCoinRatingsAsync(): Single<Map<CoinType, String>> =
        Single.create { emitter ->
            try {
                val coinRatingsMap = mutableMapOf<CoinType, String>()

                storage.getCoinInfos().forEach { coin ->
                    coin.rating?.let {
                        if (it.isNotEmpty()) {
                            coinRatingsMap[coin.coinType] = it
                        }
                    }
                }
                emitter.onSuccess(coinRatingsMap)
            } catch (error: Throwable) {
                emitter.onError(error)
            }
        }

    fun getLinks(coinType: CoinType, linksByProvider: Map<LinkType, String>): Map<LinkType, String> {
        val links = mutableMapOf<LinkType, String>()
        val linksStored = storage.getCoinLinks(coinType).map { it.linkType to it.link }.toMap()

        LinkType.values().forEach { linkType ->
            val ls = linksStored[linkType]
            val lp = linksByProvider[linkType]

            val link =
                if(ls != null && ! ls.isNullOrEmpty()) ls
                else if(lp!=null && !lp.isNullOrEmpty()) lp
                else null

            link?.let{
                links.put(linkType, link)
            }
        }

        return links
    }
}