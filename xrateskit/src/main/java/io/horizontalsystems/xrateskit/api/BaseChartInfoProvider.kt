package io.horizontalsystems.xrateskit.api

import io.horizontalsystems.xrateskit.coins.ProviderCoinsManager
import io.horizontalsystems.xrateskit.core.IChartInfoProvider
import io.horizontalsystems.xrateskit.entities.ChartInfoKey
import io.horizontalsystems.xrateskit.entities.ChartPointEntity
import io.reactivex.Single

class BaseChartInfoProvider(
    private val providerCoinsManager: ProviderCoinsManager,
    private val cryptoCompareProvider: CryptoCompareProvider,
    private val coinGeckoProvider: CoinGeckoProvider
) : IChartInfoProvider {

    override fun getChartPoints(chartPointKey: ChartInfoKey): Single<List<ChartPointEntity>> {
        val providerIds = providerCoinsManager.getProviderIds(listOf(chartPointKey.coinType), cryptoCompareProvider.provider)

        if(!providerIds.isEmpty()){
            providerIds[0]?.let {
                return cryptoCompareProvider.getChartPoints(chartPointKey)
            }
        }

        return coinGeckoProvider.getChartPoints(chartPointKey)
    }
}
