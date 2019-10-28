package io.horizontalsystems.xrateskit.chartpoint

import io.horizontalsystems.xrateskit.core.IChartInfoProvider
import io.horizontalsystems.xrateskit.entities.ChartInfoKey

class ChartInfoSchedulerFactory(
        private val manager: ChartInfoManager,
        private val provider: IChartInfoProvider,
        private val retryInterval: Long) {

    fun getScheduler(key: ChartInfoKey): ChartInfoScheduler {
        return ChartInfoScheduler(ChartInfoSchedulerProvider(retryInterval, key, provider, manager))
    }
}
