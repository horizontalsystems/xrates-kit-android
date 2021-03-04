package io.horizontalsystems.xrateskit.api

import io.horizontalsystems.xrateskit.coins.ProviderCoinsManager
import io.horizontalsystems.xrateskit.core.IChartInfoProvider
import io.horizontalsystems.xrateskit.entities.ChartInfoKey
import io.horizontalsystems.xrateskit.entities.ChartPointEntity
import io.horizontalsystems.xrateskit.entities.ChartType
import io.reactivex.Single
import java.math.BigDecimal
import java.util.*

class BaseChartInfoProvider(
    private val providerCoinsManager: ProviderCoinsManager,
    private val cryptoCompareProvider: CryptoCompareProvider,
    private val coinGeckoProvider: CoinGeckoProvider
) : IChartInfoProvider {

    override fun getChartPoints(chartPointKey: ChartInfoKey): Single<List<ChartPointEntity>> {
        val providerIds = providerCoinsManager.getProviderIds(listOf(chartPointKey.coinType), cryptoCompareProvider.provider)

        if(!providerIds.isEmpty()){
            providerIds[0]?.let {
                return cryptoCompareProvider.getChartPoints(chartPointKey).flatMap {
                    if(isChartDataValid(chartPointKey.chartType, it)) Single.just(it)
                    else coinGeckoProvider.getChartPoints(chartPointKey)
                }
            }
        }

        return coinGeckoProvider.getChartPoints(chartPointKey)
    }

    private fun isChartDataValid(chartType: ChartType, responseList: List<ChartPointEntity>): Boolean {
        val lastPoint = responseList.lastOrNull() ?: return false

        val endTimestamp = Date().time / 1000
        if (endTimestamp - chartType.rangeInterval > lastPoint.timestamp) {
            return false
        }

        val values = responseList.filter { it.value.equals(BigDecimal.ZERO) }
        if(values.size == responseList.size)
            return false

        return true
    }
}
