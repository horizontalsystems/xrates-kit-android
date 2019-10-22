package io.horizontalsystems.xrateskit.chartpoint

import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.*
import io.horizontalsystems.xrateskit.latestrate.LatestRateManager
import java.math.BigDecimal
import java.util.*

class ChartPointManager(
        private val storage: IStorage,
        private val factory: Factory,
        private val latestRateManager: LatestRateManager) {

    var listener: Listener? = null

    interface Listener {
        fun onUpdate(chartInfo: ChartInfo?, key: ChartPointKey)
    }

    fun getLastSyncTimestamp(key: ChartPointKey): Long? {
        return storedChartPoints(key).lastOrNull()?.timestamp
    }

    fun getChartInfo(key: ChartPointKey): ChartInfo? {
        val latestRate = latestRateManager.getLatestRate(key.coin, key.currency)
        return chartInfo(storedChartPoints(key), latestRate, key)
    }

    private fun chartInfo(points: List<ChartPoint>, latestRate: Rate?, key: ChartPointKey): ChartInfo? {
        val lastPoint = points.lastOrNull() ?: return null
        val firstPoint = points.first()

        val currentTimestamp = Date().time / 1000
        val lastPointDiffInterval = currentTimestamp - lastPoint.timestamp
        if (lastPointDiffInterval > key.chartType.expirationInterval) {
            return ChartInfo(
                    points,
                    firstPoint.timestamp,
                    currentTimestamp,
                    diff = null
            )
        }

        if (latestRate == null || latestRate.timestamp < lastPoint.timestamp) {
            return ChartInfo(
                    points,
                    firstPoint.timestamp,
                    lastPoint.timestamp,
                    diff = null
            )
        }

        val chartPointsWithLatestRate = points + ChartPoint(latestRate.value, latestRate.timestamp)
        var diff: BigDecimal? = null

        if (!latestRate.isExpired()) {
            diff = (latestRate.value - firstPoint.value) / firstPoint.value * BigDecimal(100)
        }

        return ChartInfo(
                chartPointsWithLatestRate,
                firstPoint.timestamp,
                latestRate.timestamp,
                diff
        )
    }

    fun update(points: List<ChartPointEntity>, key: ChartPointKey) {
        storage.deleteChartPoints(key)
        storage.saveChartPoints(points)
        listener?.onUpdate(chartInfo(points.map { ChartPoint(it.value, it.timestamp) }, key), key)
    }

    fun update(latestRate: Rate, key: ChartPointKey) {
        listener?.onUpdate(chartInfo(storedChartPoints(key), latestRate, key), key)
    }

    private fun chartInfo(points: List<ChartPoint>, key: ChartPointKey): ChartInfo? {
        val latestRate = latestRateManager.getLatestRate(key.coin, key.currency)
        return chartInfo(points, latestRate, key)
    }

    private fun storedChartPoints(key: ChartPointKey): List<ChartPoint> {
        val currentTimestamp = Date().time / 1000
        val fromTimestamp = currentTimestamp - key.chartType.rangeInterval

        return storage.getChartPoints(key, fromTimestamp).map { factory.createChartPoint(it.value, it.timestamp) }
    }
}
