package io.horizontalsystems.xrateskit.chartpoint

import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.*
import java.util.*

class ChartInfoManager(private val storage: IStorage, private val factory: Factory) {

    var listener: Listener? = null

    interface Listener {
        fun onUpdate(chartInfo: ChartInfo, key: ChartInfoKey)
        fun noChartInfo(key: ChartInfoKey)
    }

    fun getLastSyncTimestamp(key: ChartInfoKey): Long? {
        return storedChartPoints(key).lastOrNull()?.timestamp
    }

    fun getChartInfo(key: ChartInfoKey): ChartInfo? {
        return chartInfo(storedChartPoints(key), key)
    }

    private fun chartInfo(points: List<ChartPoint>, key: ChartInfoKey): ChartInfo? {
        val lastPoint = points.lastOrNull() ?: return null
        val startTimestamp = lastPoint.timestamp - key.chartType.rangeInterval

        val currentTimestamp = Date().time / 1000
        val lastPointDiffInterval = currentTimestamp - lastPoint.timestamp
        if (lastPointDiffInterval > key.chartType.expirationInterval) {
            return ChartInfo(
                points,
                startTimestamp,
                currentTimestamp
            )
        }

        return ChartInfo(
            points,
            startTimestamp,
            lastPoint.timestamp
        )
    }

    fun update(points: List<ChartPointEntity>, key: ChartInfoKey) {
        storage.deleteChartPoints(key)
        storage.saveChartPoints(points)

        val chartInfo = chartInfo(points.map { ChartPoint(it.value, it.volume, it.timestamp) }, key)
        if (chartInfo == null) {
            listener?.noChartInfo(key)
        } else {
            listener?.onUpdate(chartInfo, key)
        }
    }

    private fun storedChartPoints(key: ChartInfoKey): List<ChartPoint> {
        return storage.getChartPoints(key).map {
            factory.createChartPoint(it.value, it.volume, it.timestamp)
        }
    }

    fun handleNoChartPoints(key: ChartInfoKey) {
        listener?.noChartInfo(key)
    }
}
