package io.horizontalsystems.xrateskit.chartpoint

import io.horizontalsystems.xrateskit.core.IChartPointProvider
import io.horizontalsystems.xrateskit.entities.ChartPointKey
import io.reactivex.Single

class ChartPointSchedulerProvider(
        val retryInterval: Long,
        private val key: ChartPointKey,
        private val provider: IChartPointProvider,
        private val manager: ChartPointManager) {

    val lastSyncTimestamp: Long?
        get() = manager.getLastSyncTimestamp(key)

    val expirationInterval: Long
        get() = key.chartType.seconds

    val syncSingle: Single<Unit>
        get() = provider.getChartPoints(key)
                .doOnSuccess { points ->
                    manager.update(points, key)
                }
                .map { Unit }
}
