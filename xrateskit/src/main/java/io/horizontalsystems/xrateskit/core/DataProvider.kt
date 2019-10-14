package io.horizontalsystems.xrateskit.core

import io.horizontalsystems.xrateskit.entities.*
import io.horizontalsystems.xrateskit.managers.ChartStatsSyncer
import io.horizontalsystems.xrateskit.managers.HistoricalRateManager
import io.horizontalsystems.xrateskit.managers.LatestRateSyncer
import io.horizontalsystems.xrateskit.managers.MarketStatsManager
import io.reactivex.Single
import java.math.BigDecimal

class DataProvider(
        private val storage: IStorage,
        private val factory: Factory,
        private val subjectHolder: SubjectHolder,
        private val chartStatsSyncer: ChartStatsSyncer,
        private val historicalRateManager: HistoricalRateManager,
        private val marketStatsManager: MarketStatsManager)
    : LatestRateSyncer.Listener, ChartStatsSyncer.Listener {

    fun getLatestRate(coin: String, currency: String): RateInfo? {
        return storage.getLatestRate(coin, currency)?.let {
            factory.createRateInfo(it)
        }
    }

    fun getChartPoints(coin: String, currency: String, chartType: ChartType): List<ChartPoint> {
        val chartStats = storage.getChartStats(coin, currency, chartType)

        val lastChartStats = chartStats.lastOrNull()
        if (lastChartStats == null || chartType.isExpired(lastChartStats.timestamp)) {
            chartStatsSyncer.sync(coin, currency, chartType)
        }

        return pointsWithLatestRate(chartStats, storage.getLatestRate(coin, currency))
    }

    fun getMarketStats(coin: String, currency: String): Single<MarketStatsInfo> {
        return marketStatsManager.getMarketStats(coin, currency).map {
            factory.createMarketCapInfo(it)
        }
    }

    fun getHistoricalRate(coin: String, currency: String, timestamp: Long): Single<BigDecimal> {
        return historicalRateManager.getHistoricalRate(coin, currency, timestamp)
    }

    //  LatestRateSyncer Listener

    override fun onUpdate(latestRate: LatestRate) {
        val rateInfo = factory.createRateInfo(latestRate)
        val rateSubjectKey = LatestRateSubjectKey(latestRate.coin, latestRate.currency)

        subjectHolder.latestRateSubject[rateSubjectKey]?.onNext(rateInfo)

        //  Update chart stats
        val filteredChartStatsKeys = subjectHolder.activeChartStatsKeys.filter { it.coin == latestRate.coin }

        for (subjectKey in filteredChartStatsKeys) {
            val chartStats = storage.getChartStats(subjectKey.coin, subjectKey.currency, subjectKey.chartType)
            if (chartStats.isEmpty()) {
                continue
            }

            val subject = subjectHolder.chartStatsSubject[subjectKey] ?: continue
            subject.onNext(pointsWithLatestRate(chartStats, latestRate))
        }
    }

    //  ChartStatsManager Listener

    override fun onUpdate(stats: List<ChartStats>, coin: String, currency: String, chartType: ChartType) {
        val statsSubjectKey = ChartStatsSubjectKey(coin, currency, chartType)

        val publishSubject = subjectHolder.chartStatsSubject[statsSubjectKey]
        if (publishSubject == null || stats.isEmpty()) {
            return
        }

        val latestRate = storage.getLatestRate(coin, currency)
        publishSubject.onNext(pointsWithLatestRate(stats, latestRate))
    }

    private fun pointsWithLatestRate(stats: List<ChartStats>, latestRate: LatestRate?): List<ChartPoint> {
        val chartPoints = stats.map { factory.createChartPoint(it.value, it.timestamp) }

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
