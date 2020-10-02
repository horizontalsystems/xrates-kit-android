package io.horizontalsystems.xrateskit.chartpoint

import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.*
import io.horizontalsystems.xrateskit.marketinfo.MarketInfoManager
import java.util.*

class ChartInfoManager(private val storage: IStorage, private val factory: Factory, private val marketInfoManager: MarketInfoManager) {

    var listener: Listener? = null

    interface Listener {
        fun onUpdate(chartInfo: ChartInfo, key: ChartInfoKey)
        fun noChartInfo(key: ChartInfoKey)
    }

    fun getLastSyncTimestamp(key: ChartInfoKey): Long? {
        return storedChartPoints(key).lastOrNull()?.timestamp
    }

    fun getChartInfo(key: ChartInfoKey): ChartInfo? {
        return chartInfo(storedChartPoints(key), key.chartType)
    }

    private fun chartInfo(points: List<ChartPoint>, chartType: ChartType): ChartInfo? {
        val lastPoint = points.lastOrNull() ?: return null

        var endTimestamp = Date().time / 1000
        if (endTimestamp - chartType.rangeInterval > lastPoint.timestamp) {
            return null
        }

        val startTimestamp: Long
        if (chartType === ChartType.TODAY) {
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
            startTimestamp = calendar.timeInMillis / 1000

            val day = 24 * 60 * 60
            endTimestamp = startTimestamp + day
        } else {
            startTimestamp = lastPoint.timestamp - chartType.rangeInterval
        }

        if (endTimestamp - chartType.expirationInterval > lastPoint.timestamp) {
            return ChartInfo(
                points,
                startTimestamp,
                endTimestamp = endTimestamp,
                isExpired = true
            )
        }

        return ChartInfo(
            points,
            startTimestamp,
            endTimestamp = lastPoint.timestamp,
            isExpired = false
        )
    }

    fun update(points: List<ChartPointEntity>, key: ChartInfoKey) {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("GMT"))

        val startDayTimestamp = calendar.timeInMillis / 1000
        val entities = points.map { point ->
            if (point.timestamp == startDayTimestamp) {
                marketInfoManager.getMarketInfo(point.coin, point.currency)?.let { marketInfo ->
                    return@map ChartPointEntity(
                        point.type,
                        point.coin,
                        point.currency,
                        marketInfo.rateOpenDay,
                        point.volume,
                        point.timestamp
                    )
                }
            }

            return@map point
        }

        storage.deleteChartPoints(key)
        storage.saveChartPoints(entities)

        val chartInfo = chartInfo(entities.map { ChartPoint(it.value, it.volume, it.timestamp) }, key.chartType)
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
