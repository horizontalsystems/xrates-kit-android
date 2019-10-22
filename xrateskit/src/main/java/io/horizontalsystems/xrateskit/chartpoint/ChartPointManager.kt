package io.horizontalsystems.xrateskit.chartpoint

import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.ChartPoint
import io.horizontalsystems.xrateskit.entities.ChartPointInfo
import io.horizontalsystems.xrateskit.entities.ChartPointKey

class ChartPointManager(
        private val storage: IStorage,
        private val factory: Factory) {

    var listener: Listener? = null

    interface Listener {
        fun onUpdate(points: List<ChartPointInfo>, key: ChartPointKey)
    }

    fun getLastSyncTimestamp(key: ChartPointKey): Long? {
        return storage.getLatestChartPoints(key)?.timestamp
    }

    fun getChartPoints(key: ChartPointKey): List<ChartPointInfo> {
        val stats = storage.getChartPoints(key)
        return pointsWithLatestRate(stats, key)
    }

    fun update(points: List<ChartPoint>, key: ChartPointKey) {
        storage.deleteChartPoints(key)
        storage.saveChartPoints(points)
        listener?.onUpdate(pointsWithLatestRate(points, key), key)
    }

    private fun pointsWithLatestRate(points: List<ChartPoint>, key: ChartPointKey): List<ChartPointInfo> {
        val latestRate = storage.getLatestRate(key.coin, key.currency)
        val chartPoints = points.map { factory.createChartPoint(it.value, it.timestamp) }

        val lastPoint = chartPoints.lastOrNull()
        if (lastPoint == null || latestRate == null) {
            return chartPoints
        }

        //  Skip adding latest rate to charts if expired
        if (lastPoint.timestamp >= latestRate.timestamp) {
            return chartPoints
        }

        return chartPoints + factory.createChartPoint(latestRate.value, latestRate.timestamp)
    }
}
