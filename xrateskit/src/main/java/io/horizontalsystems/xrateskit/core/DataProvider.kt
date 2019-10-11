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

    fun getChartPoints(coin: String, currency: String, type: ChartType): List<ChartPoint> {
        val chartStats = storage.getChartStats(coin, currency, type)

        val lastChartStats = chartStats.lastOrNull()
        if (lastChartStats == null || lastChartStats.timestamp < 0L) {
            chartStatsSyncer.syncChartStats(coin, currency, type)
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

    override fun onUpdate(rate: LatestRate) {
        val rateInfo = factory.createRateInfo(rate)
        val rateSubjectKey = LatestRateSubjectKey(rate.coin, rate.currency)

        subjectHolder.latestRateSubject[rateSubjectKey]?.onNext(rateInfo)

        //  Update chart stats
        val filteredChartStatsKeys = subjectHolder.activeChartStatsKeys.filter { it.coin == rate.coin }

        for (subjectKey in filteredChartStatsKeys) {
            val chartStats = storage.getChartStats(subjectKey.coin, subjectKey.currency, subjectKey.type)
            if (chartStats.isEmpty()) {
                continue
            }

            val subject = subjectHolder.chartStatsSubject[subjectKey] ?: continue
            subject.onNext(pointsWithLatestRate(chartStats, rate))
        }
    }

    //  ChartStatsManager Listener

    override fun onUpdate(stats: List<ChartStats>, coin: String, currency: String, type: ChartType) {
        val statsSubjectKey = ChartStatsSubjectKey(coin, currency, type)

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
