package io.horizontalsystems.xrateskit.chartpoint

import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.*
import io.horizontalsystems.xrateskit.marketinfo.MarketInfoManager
import java.util.*

class ChartInfoManager(
        private val storage: IStorage,
        private val factory: Factory,
        private val latestRateManager: MarketInfoManager) {

    var listener: Listener? = null

    interface Listener {
        fun onUpdate(chartInfo: ChartInfo, key: ChartInfoKey)
        fun noChartInfo(key: ChartInfoKey)
    }

    fun getLastSyncTimestamp(key: ChartInfoKey): Long? {
        return storedChartPoints(key).lastOrNull()?.timestamp
    }

    fun getChartInfo(key: ChartInfoKey): ChartInfo? {
        val marketInfo = latestRateManager.getMarketInfo(key.coin, key.currency)
        return chartInfo(storedChartPoints(key), marketInfo, key)
    }

    private fun chartInfo(points: List<ChartPoint>, marketInfo: MarketInfo?, key: ChartInfoKey): ChartInfo? {
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

        if (marketInfo == null) {
            return ChartInfo(
                    points,
                    firstPoint.timestamp,
                    lastPoint.timestamp
            )
        }

        var firstTimestamp = firstPoint.timestamp
        var chartPoints = points.filter { it.timestamp < marketInfo.timestamp }
        if (key.chartType == ChartType.DAILY) {
            firstTimestamp = marketInfo.timestamp - key.chartType.rangeInterval
            chartPoints = listOf(ChartPoint(marketInfo.rateOpen24Hour, firstTimestamp)) + chartPoints.filter { it.timestamp > firstTimestamp }
        }

        val chartPointsWithLatestRate = chartPoints + ChartPoint(marketInfo.rate, marketInfo.timestamp)

        return ChartInfo(
                chartPointsWithLatestRate,
                firstTimestamp,
                marketInfo.timestamp
        )
    }

    fun update(points: List<ChartPointEntity>, key: ChartInfoKey) {
        storage.deleteChartPoints(key)
        storage.saveChartPoints(points)

        val chartInfo = chartInfo(points.map { ChartPoint(it.value, it.timestamp) }, key)
        if (chartInfo == null) {
            listener?.noChartInfo(key)
        } else {
            listener?.onUpdate(chartInfo, key)
        }
    }

    fun update(marketInfo: MarketInfo, key: ChartInfoKey) {
        chartInfo(storedChartPoints(key), marketInfo, key)?.let {
            listener?.onUpdate(it, key)
        }
    }

    private fun chartInfo(points: List<ChartPoint>, key: ChartInfoKey): ChartInfo? {
        val latestRate = latestRateManager.getMarketInfo(key.coin, key.currency)
        return chartInfo(points, latestRate, key)
    }

    private fun storedChartPoints(key: ChartInfoKey): List<ChartPoint> {
        val currentTimestamp = Date().time / 1000
        val fromTimestamp = currentTimestamp - key.chartType.rangeInterval

        return storage.getChartPoints(key, fromTimestamp).map { factory.createChartPoint(it.value, it.timestamp) }
    }

    fun handleNoChartPoints(key: ChartInfoKey) {
        listener?.noChartInfo(key)
    }
}
