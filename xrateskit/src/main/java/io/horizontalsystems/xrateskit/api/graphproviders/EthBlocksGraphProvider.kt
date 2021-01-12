package io.horizontalsystems.xrateskit.api.graphproviders

import io.horizontalsystems.xrateskit.api.ApiManager
import io.horizontalsystems.xrateskit.api.InfoProvider
import io.horizontalsystems.xrateskit.core.IInfoProvider
import io.horizontalsystems.xrateskit.entities.EthBlocksGraphResponse
import io.horizontalsystems.xrateskit.entities.TimePeriod
import io.reactivex.Single

class EthBlocksGraphProvider(
    private val apiManager: ApiManager )
    : IInfoProvider {

    override val provider: InfoProvider = InfoProvider.GraphNetwork()
    private val SUB_URL = "/blocklytics/ethereum-blocks"

    init {
        this.provider.baseUrl += SUB_URL
    }

    override fun initProvider() {}
    override fun destroy() {}

    fun getBlockHeight(data: Map<TimePeriod, Long>) :Single<Map<TimePeriod,Long>> {
        return Single.create { emitter ->
            try {
                val responseData = apiManager.getJson( this.provider.baseUrl, GraphQueryBuilder.buildBlockHeightQuery(data))

                emitter.onSuccess(EthBlocksGraphResponse.parseData(responseData))

            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }
}