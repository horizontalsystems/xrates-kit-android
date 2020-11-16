package io.horizontalsystems.xrateskit.api.uniswapgraph

import com.eclipsesource.json.JsonObject
import io.horizontalsystems.xrateskit.entities.Coin

class GraphQueryBuilder {

    companion object {

        fun buildLatestXRatesQuery(coins: List<Coin>): String {
            return buildQuery(buildTokensQuery(coins))
        }

        fun buildETHPriceQuery(): String {
            return buildQuery(buildBundleQuery())
        }

        fun buildHistoricalXRatesQuery(coins: List<Coin>, timeStamp: Long ): String {
            return buildQuery(buildTokenDayDatasQuery(coins, timeStamp))
        }

        private fun buildTokensQuery(coins: List<Coin>): String {

            val addresses = coins.joinToString { coin -> "\"${coin.address}\"" }.toLowerCase()
            return """
                    tokens( 
                    where : {id_in: [ $addresses ]})
                    { symbol,
                      derivedETH,
                    }
                    """
        }

        private fun buildBundleQuery(): String {
            return "bundle( id:1 ) { ethPriceUSD: ethPrice }"
        }

        private fun buildTokenDayDatasQuery(coins: List<Coin>, timeStamp: Long): String {

            var query = ""
            coins.forEach { coin ->
                query += """${coin.coinId}:tokenDayDatas(
                        first:1,
                        orderBy:date,
                        orderDirection:desc,
                        where :{  
                          date_lte:${timeStamp},
                          token: "${coin.address.toLowerCase()}"})
                        { date,
                          priceUSD
                        }
                        """
            }
            return query
        }

        private fun buildQuery(query: String, variables: String? = null): String {
            return JsonObject()
                .add("query", "{ $query }")
                .add("variables", variables).toString()
        }
    }
}
