package io.horizontalsystems.xrateskit.api.graphprovider

import com.eclipsesource.json.JsonObject

class GraphQueryBuilder {

    companion object {

        fun buildETHPriceQuery(): String {
            return buildQuery(buildBundleQuery())
        }

        fun buildHistoricalXRatesQuery(tokenAddresses: List<String>, timeStamp: Long ): String {
            return buildQuery(buildTokenDayDatasQuery(tokenAddresses, timeStamp))
        }

        private fun buildBundleQuery(): String {
            return "bundle( id:1 ) { ethPriceUSD: ethPrice }".trimIndent()
        }

        private fun buildTokenDayDatasQuery(tokenAddresses: List<String>, timeStamp: Long): String {

            var query = ""
            tokenAddresses.forEachIndexed { index, address ->
                    query += """o${index}:tokenDayDatas(
                        first:1,
                        orderBy:date,
                        orderDirection:desc,
                        where :{  
                          date_lte:${timeStamp},
                          token: "${address}"})
                          { 
                             token { symbol, derivedETH },
                             priceUSD
                          }""".trimIndent()
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
