package io.horizontalsystems.xrateskit.api

import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.ITopMarketsProvider
import io.horizontalsystems.xrateskit.entities.Coin
import io.horizontalsystems.xrateskit.entities.TopMarket
import io.reactivex.Single
import java.math.BigDecimal
import java.util.logging.Logger

class CoinGeckoProvider(
    private val factory: Factory,
    private val apiManager: ApiManager
): ITopMarketsProvider {

        private val logger = Logger.getLogger("CoinGeckoProvider")
        private val BASE_URL = "https://api.coingecko.com/api/v3"

        override fun getTopMarkets(itemsCount: Int, currencyCode: String): Single<List<TopMarket>> {
            return Single.create { emitter ->
                try {
                    val json = apiManager.getJsonValue("$BASE_URL/coins/markets?vs_currency=${currencyCode}&price_change_percentage=1h,7d,30d,1y&order=market_cap_desc&per_page=${itemsCount}")
                    val topMarkets = mutableListOf<TopMarket>()
                    json.asArray()?.forEach { marketData ->
                        marketData?.asObject()?.let { element ->
                            val coinCode = element.get("symbol").asString()
                            val title = element.get("name").asString()
                            val rate = element.get("current_price").asDouble().toBigDecimal()
                            val rateOpenDay = rate + element.get("price_change_24h").asDouble().toBigDecimal()
                            val supply = element.get("circulating_supply").asDouble().toBigDecimal()
                            val volume = element.get("total_volume").asDouble().toBigDecimal()
                            val marketCap = element.get("market_cap").asDouble().toBigDecimal()

                            val rateDiff1h =
                                if(element.get("price_change_percentage_1h_in_currency").isNull) BigDecimal.ZERO
                                else element.get("price_change_percentage_1h_in_currency").asDouble().toBigDecimal()
                            val rateDiff24h =
                                if(element.get("price_change_percentage_24h").isNull) BigDecimal.ZERO
                                else element.get("price_change_percentage_24h").asDouble().toBigDecimal()
                            val rateDiff7d =
                                if(element.get("price_change_percentage_7d_in_currency").isNull) BigDecimal.ZERO
                                else element.get("price_change_percentage_7d_in_currency").asDouble().toBigDecimal()
                            val rateDiff30d = if(element.get("price_change_percentage_30d_in_currency").isNull) BigDecimal.ZERO
                                else element.get("price_change_percentage_30d_in_currency").asDouble().toBigDecimal()
                            val rateDiff1y =
                                if(element.get("price_change_percentage_1y_in_currency").isNull) BigDecimal.ZERO
                                else element.get("price_change_percentage_1y_in_currency").asDouble().toBigDecimal()

                            topMarkets.add(factory.createTopMarket(
                                Coin(coinCode, title), currencyCode, rate,
                                rateOpenDay, rateDiff24h,
                                volume, marketCap, supply, BigDecimal.ZERO, rateDiff1h, rateDiff24h, rateDiff7d,
                                rateDiff30d,
                                rateDiff1y))
                        }
                    }

                    emitter.onSuccess(topMarkets)

                } catch (ex: Exception) {
                    logger.severe(ex.message)
                    emitter.onError(ex)
                }
            }
        }
    }