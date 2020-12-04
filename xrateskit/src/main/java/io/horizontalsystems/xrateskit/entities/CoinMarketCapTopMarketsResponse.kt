package io.horizontalsystems.xrateskit.entities

import com.eclipsesource.json.JsonObject

data class CoinMarketCapTopMarketsResponse(val values: List<Coin>) {

    companion object {
        fun parseData(jsonObject: JsonObject): CoinMarketCapTopMarketsResponse {
            val coins = mutableListOf<Coin>()

            jsonObject.get("data")?.asArray()?.forEach { marketData ->
                marketData?.asObject()?.let { element ->
                    val coinCode = element.get("symbol").asString()
                    val title = element.get("name").asString()
                    var type: CoinType? = null

                    if (!element.get("platform").isNull) {
                        element.get("platform").let { platform ->
                            platform.asObject()?.get("name")?.asString()?.let {
                                if (it.contentEquals("Ethereum")) {
                                    type = CoinType.Erc20(platform.asObject().get("token_address").asString())
                                }
                            }
                        }
                    }

                    coins.add(Coin(coinCode, title, type))
                }
            }

            return CoinMarketCapTopMarketsResponse(coins)
        }
    }
}