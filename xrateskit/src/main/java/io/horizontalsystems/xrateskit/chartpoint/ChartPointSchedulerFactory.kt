package io.horizontalsystems.xrateskit.chartpoint

import io.horizontalsystems.xrateskit.core.IChartPointProvider
import io.horizontalsystems.xrateskit.entities.ChartPointKey

class ChartPointSchedulerFactory(
        private val manager: ChartPointManager,
        private val provider: IChartPointProvider,
        private val retryInterval: Long) {

    fun getScheduler(key: ChartPointKey): ChartPointScheduler {
        return ChartPointScheduler(ChartPointSchedulerProvider(retryInterval, key, provider, manager))
    }
}
