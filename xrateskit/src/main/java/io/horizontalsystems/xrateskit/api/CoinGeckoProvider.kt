package io.horizontalsystems.xrateskit.api

import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.ITopMarketsProvider
import io.horizontalsystems.xrateskit.entities.Coin
import io.horizontalsystems.xrateskit.entities.TimePeriod
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

        override fun getTopMarketsAsync(currencyCode: String, fetchDiffPeriod: TimePeriod, itemsCount: Int): Single<List<TopMarket>> {
            return Single.create { emitter ->
                try {
                    val json = apiManager.getJsonValue("$BASE_URL/coins/markets?vs_currency=${currencyCode}&price_change_percentage=1h,7d,30d,1y&order=market_cap_desc&per_page=${itemsCount}")
                    val topMarkets = mutableListOf<TopMarket>()
                    json.asArray()?.forEach {  marketData ->
                        marketData?.asObject()?.let { element ->
                            val coinCode = element.get("symbol").asString()
                            val title = element.get("name").asString()

                            val rate = if(element.get("current_price").isNull) BigDecimal.ZERO
                                       else element.get("current_price").asDouble().toBigDecimal()

                            val rateOpenDay = if(element.get("price_change_24h").isNull) BigDecimal.ZERO
                                              else rate + element.get("price_change_24h").asDouble().toBigDecimal()

                            val supply = if(element.get("circulating_supply").isNull) BigDecimal.ZERO
                                         else element.get("circulating_supply").asDouble().toBigDecimal()

                            val volume = if(element.get("total_volume").isNull) BigDecimal.ZERO
                                         else element.get("total_volume").asDouble().toBigDecimal()
                            val marketCap = if(element.get("market_cap").isNull) BigDecimal.ZERO
                                            else element.get("market_cap").asDouble().toBigDecimal()

                            val priceDiffFieldName =
                                when(fetchDiffPeriod){
                                    TimePeriod.DAY_7 -> "price_change_percentage_7d_in_currency"
                                    TimePeriod.HOUR_1 -> "price_change_percentage_1h_in_currency"
                                    TimePeriod.HOUR_24 -> "price_change_percentage_24h"
                                    TimePeriod.DAY_30 -> "price_change_percentage_30d_in_currency"
                                    TimePeriod.YEAR_1 -> "price_change_percentage_1y_in_currency"
                                    else ->  "price_change_percentage_24h"
                                }

                            val rateDiffPeriod =
                                if(element.get(priceDiffFieldName).isNull) BigDecimal.ZERO
                                else element.get(priceDiffFieldName).asDouble().toBigDecimal()

                            val rateDiff24h =
                                if(element.get("price_change_percentage_24h").isNull) BigDecimal.ZERO
                                else element.get("price_change_percentage_24h").asDouble().toBigDecimal()

                            topMarkets.add(factory.createTopMarket(
                                Coin(coinCode, title), currencyCode, rate,
                                rateOpenDay, rateDiff24h,
                                volume, marketCap, supply, BigDecimal.ZERO, rateDiffPeriod))
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