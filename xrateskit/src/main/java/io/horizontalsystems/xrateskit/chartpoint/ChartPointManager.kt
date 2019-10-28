package io.horizontalsystems.xrateskit.chartpoint

import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.*
import io.horizontalsystems.xrateskit.marketinfo.MarketInfoManager
import java.util.*

class ChartPointManager(
        private val storage: IStorage,
        private val factory: Factory,
        private val latestRateManager: MarketInfoManager) {

    var listener: Listener? = null

    interface Listener {
        fun onUpdate(chartInfo: ChartInfo, key: ChartPointKey)
    }

    fun getLastSyncTimestamp(key: ChartPointKey): Long? {
        return storedChartPoints(key).lastOrNull()?.timestamp
    }

    fun getChartInfo(key: ChartPointKey): ChartInfo? {
        val marketInfo = latestRateManager.getMarketInfo(key.coin, key.currency)
        return chartInfo(storedChartPoints(key), marketInfo, key)
    }

    private fun chartInfo(points: List<ChartPoint>, marketInfo: MarketInfo?, key: ChartPointKey): ChartInfo? {
        val lastPoint = points.lastOrNull() ?: return null
        val firstPoint = points.first()

        val currentTimestamp = Date().time / 1000
        val lastPointDiffInterval = currentTimestamp - lastPoint.timestamp
        if (lastPointDiffInterval > key.chartType.expirationInterval) {
            return ChartInfo(
                    points,
                    firstPoint.timestamp,
                    currentTimestamp
            )
        }

        if (marketInfo == null || marketInfo.timestamp < lastPoint.timestamp) {
            return ChartInfo(
                    points,
                    firstPoint.timestamp,
                    lastPoint.timestamp
            )
        }

        val chartPointsWithLatestRate = points + ChartPoint(marketInfo.rate, marketInfo.timestamp)

        return ChartInfo(
                chartPointsWithLatestRate,
                firstPoint.timestamp,
                marketInfo.timestamp
        )
    }

    fun update(points: List<ChartPointEntity>, key: ChartPointKey) {
        storage.deleteChartPoints(key)
        storage.saveChartPoints(points)
        chartInfo(points.map { ChartPoint(it.value, it.timestamp) }, key)?.let {
            listener?.onUpdate(it, key)
        }
    }

    fun update(marketInfo: MarketInfo, key: ChartPointKey) {
        chartInfo(storedChartPoints(key), marketInfo, key)?.let {
            listener?.onUpdate(it, key)
        }
    }

    private fun chartInfo(points: List<ChartPoint>, key: ChartPointKey): ChartInfo? {
        val latestRate = latestRateManager.getMarketInfo(key.coin, key.currency)
        return chartInfo(points, latestRate, key)
    }

    private fun storedChartPoints(key: ChartPointKey): List<ChartPoint> {
        val currentTimestamp = Date().time / 1000
        val fromTimestamp = currentTimestamp - key.chartType.rangeInterval

        return storage.getChartPoints(key, fromTimestamp).map { factory.createChartPoint(it.value, it.timestamp) }
    }
}
