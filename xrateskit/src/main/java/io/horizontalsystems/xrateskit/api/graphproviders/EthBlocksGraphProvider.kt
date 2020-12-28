package io.horizontalsystems.xrateskit.api.graphproviders

import io.horizontalsystems.xrateskit.api.ApiManager
import io.horizontalsystems.xrateskit.entities.EthBlocksGraphResponse
import io.horizontalsystems.xrateskit.entities.TimePeriod
import io.reactivex.Single
import java.sql.Time

class EthBlocksGraphProvider(
    private val apiManager: ApiManager ) {

    //https://api.thegraph.com/subgraphs/name/ayogatot/griffin-ethereum-blocks
    private val BASE_URL = "https://api.thegraph.com/subgraphs/name/blocklytics/ethereum-blocks"

    fun getBlockHeight(data: Map<TimePeriod, Long>) :Single<Map<TimePeriod,Long>> {
        return Single.create { emitter ->
            try {
                var blockHeight = 0.toLong()

                val responseData = apiManager.getJson( BASE_URL, GraphQueryBuilder.buildBlockHeightQuery(data))

                emitter.onSuccess(EthBlocksGraphResponse.parseData(responseData))

            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

}