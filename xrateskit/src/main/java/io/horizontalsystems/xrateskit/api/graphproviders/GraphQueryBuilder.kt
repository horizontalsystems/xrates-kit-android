package io.horizontalsystems.xrateskit.api.graphproviders

import com.eclipsesource.json.JsonObject
import io.horizontalsystems.xrateskit.entities.TimePeriod
import java.util.*

class GraphQueryBuilder {

    companion object {

        fun buildETHPriceQuery(): String {
            return buildQuery(buildBundlesQuery())
        }

        fun buildTopTokensQuery(itemsCount:Int, blockHeight: Long? = null): String {
            return buildQuery("${buildBundlesQuery(blockHeight)}, ${buildTokensQuery(itemsCount, blockHeight)}")
        }

        fun buildMarketInfoQuery(tokenAddresses: List<String>, blockHeight: Long? = null): String {
            return buildQuery("${buildBundlesQuery(blockHeight)}, ${buildTokensQuery(tokenAddresses, blockHeight)}")
        }

        fun buildBlockHeightQuery(data : Map<TimePeriod,Long>): String {

            var query = ""
            data.entries.forEach {
                query += "${buildBlocksQuery(it.key.toString(), it.value)},"
            }
            return buildQuery(query)
        }

        fun buildHistoricalXRatesQuery(tokenAddresses: List<String>, timeStamp: Long ): String {
            return buildQuery(buildTokenDayDatasQuery(tokenAddresses, timeStamp))
        }

        private fun buildBlocksQuery(tag: String, timeStamp: Long): String {
            val timeStampLowRange = timeStamp - 60 // timeStamp minus 1 min
            return """${tag}:blocks(  
                        first: 1, 
                        where:{timestamp_lte:${timeStamp},timestamp_gte:${timeStampLowRange}}) 
                        {
                           number
                        }""".trimIndent()
        }

        private fun buildBundlesQuery(blockHeight: Long? = null): String {
            val blockNumberFilter = if(blockHeight != null) "(block:{number:${blockHeight}})" else ""

            return "bundles${blockNumberFilter} { ethPriceUSD: ethPrice }".trimIndent()
        }

        private fun buildTokensQuery(itemsCount:Int, blockHeight: Long? = null): String {

            val blockNumberFilter = if(blockHeight != null) ",block:{number:${blockHeight}}" else ""

            return """tokens(
                       first:${itemsCount}, 
                       orderBy:tradeVolumeUSD, 
                       orderDirection:desc,
                        where:{
                            totalLiquidity_not:0,
                            tradeVolumeUSD_gt:5000,
                            derivedETH_not:0,
                            symbol_not:""
                       }
                       ${blockNumberFilter})
                       {  id,
                          symbol,
                          name,
                          derivedETH,
                          tradeVolumeUSD,
                          totalLiquidity
                       }""".trimIndent()
        }

        private fun buildTokensQuery(tokenAddresses: List<String>, blockHeight: Long? = null): String {

            val blockNumberFilter = if(blockHeight != null) ",block:{number:${blockHeight}}" else ""
            val addresses = tokenAddresses.joinToString { "\"${ it }\"" }
                .toLowerCase(Locale.getDefault())

            return """tokens(
                       where:{id_in:[$addresses]}
                       ${blockNumberFilter})
                       {  id,
                          symbol,
                          name,
                          derivedETH,
                          tradeVolumeUSD,
                          totalLiquidity
                       }""".trimIndent()
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
