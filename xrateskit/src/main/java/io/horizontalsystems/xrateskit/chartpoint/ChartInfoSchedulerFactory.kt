package io.horizontalsystems.xrateskit.chartpoint

import io.horizontalsystems.xrateskit.core.IChartInfoProvider
import io.horizontalsystems.xrateskit.entities.ChartInfoKey
import io.horizontalsystems.xrateskit.scheduler.Scheduler

class ChartInfoSchedulerFactory(
        private val manager: ChartInfoManager,
        private val provider: IChartInfoProvider,
        private val retryInterval: Long) {

    fun getScheduler(key: ChartInfoKey): Scheduler {
        return Scheduler(ChartInfoSchedulerProvider(retryInterval, key, provider, manager))
    }
}
