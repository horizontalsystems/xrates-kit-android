package io.horizontalsystems.xrateskit.api

import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.IMarketInfoProvider
import io.horizontalsystems.xrateskit.core.ITopMarketsProvider
import io.horizontalsystems.xrateskit.entities.Coin
import io.horizontalsystems.xrateskit.entities.CoinType
import io.horizontalsystems.xrateskit.entities.TopMarket
import io.horizontalsystems.xrateskit.entities.TopMarketCoin
import io.reactivex.Single
import java.lang.Exception
import java.util.logging.Logger

class CoinMarketCapProvider(
        private val factory: Factory,
        private val apiManager: ApiManager,
        private val baseUrl: String,
        private val topMarketsCount: Int,
        private val apiKey: String,
        private val marketInfoProvider: IMarketInfoProvider
) : ITopMarketsProvider {

    private val logger = Logger.getLogger("CoinMarketCapProvider")

    override fun getTopMarkets(currency: String): Single<List<TopMarket>> {
        return Single.create<List<TopMarketCoin>> { emitter ->
            try {
                val json = apiManager.getJson("$baseUrl/listings/latest?limit=$topMarketsCount", mapOf("X-CMC_PRO_API_KEY" to apiKey))
                val data = json["data"].asArray()
                val topMarketCoins = data.map {
                    val coinData = it.asObject()
                    TopMarketCoin(coinData["symbol"].asString(), coinData["name"].asString())
                }
                emitter.onSuccess(topMarketCoins)
            } catch (ex: Exception) {
                logger.severe(ex.message)
                emitter.onError(ex)
            }
        }.flatMap { topMarketCoins ->
            marketInfoProvider.getMarketInfo(topMarketCoins.map { Coin(it.code, it.name, it.code) }, currency)
                    .map { marketInfos ->
                        topMarketCoins.mapNotNull { coin ->
                            marketInfos.firstOrNull { it.coin == coin.code }?.let { marketInfo ->
                                factory.createTopMarket(coin, marketInfo)
                            }
                        }
                    }
        }
    }

}