package io.horizontalsystems.xrateskit.chartpoint

import io.horizontalsystems.xrateskit.api.ProviderError
import io.horizontalsystems.xrateskit.core.IChartInfoProvider
import io.horizontalsystems.xrateskit.entities.ChartInfoKey
import io.reactivex.Single

class ChartInfoSchedulerProvider(
        val retryInterval: Long,
        private val key: ChartInfoKey,
        private val provider: IChartInfoProvider,
        private val manager: ChartInfoManager) {

    val lastSyncTimestamp: Long?
        get() = manager.getLastSyncTimestamp(key)

    val expirationInterval: Long
        get() = key.chartType.seconds

    val syncSingle: Single<Unit>
        get() = provider.getChartPoints(key)
                .doOnSuccess { points ->
                    manager.update(points, key)
                }
                .doOnError {
                    if (it is ProviderError.NoDataForCoin) {
                        manager.handleNoChartPoints(key)
                    }
                }
                .map { Unit }
}
