package io.horizontalsystems.xrateskit.chartpoint

import io.horizontalsystems.xrateskit.providers.ProviderError
import io.horizontalsystems.xrateskit.core.IChartInfoProvider
import io.horizontalsystems.xrateskit.entities.ChartInfoKey
import io.horizontalsystems.xrateskit.scheduler.ISchedulerProvider
import io.reactivex.Single

class ChartInfoSchedulerProvider(
    override val retryInterval: Long,
    private val key: ChartInfoKey,
    private val provider: IChartInfoProvider,
    private val manager: ChartInfoManager
) : ISchedulerProvider {

    override val id = key.toString()

    override val lastSyncTimestamp: Long?
        get() = manager.getLastSyncTimestamp(key)

    override val expirationInterval: Long
        get() = key.chartType.seconds

    override val syncSingle: Single<Unit>
        get() = provider.getChartPointsAsync(key)
            .doOnSuccess { points ->
                manager.update(points, key)
            }
            .doOnError {
                if (it is ProviderError.NoDataForCoin) {
                    manager.handleNoChartPoints(key)
                }
            }
            .map { Unit }

    override fun notifyExpired() = Unit

}
