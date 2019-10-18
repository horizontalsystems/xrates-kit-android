package io.horizontalsystems.xrateskit.managers

import io.horizontalsystems.xrateskit.XRatesDataSource
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.IChartStatsProvider
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.core.SubjectHolder
import io.horizontalsystems.xrateskit.entities.ChartStats
import io.horizontalsystems.xrateskit.entities.ChartType
import io.reactivex.Single
import io.reactivex.disposables.Disposable

class ChartStatsSyncer(
        private val factory: Factory,
        private val storage: IStorage,
        private val dataSource: XRatesDataSource,
        private val subjectHolder: SubjectHolder,
        private val statsProvider: IChartStatsProvider) {

    var listener: Listener? = null

    private var disposable: Disposable? = null

    interface Listener {
        fun onUpdate(stats: List<ChartStats>, coin: String, currency: String, chartType: ChartType)
    }

    val lastSyncTimestamp: Long?
        get() = lastSyncTimestamp()

    val syncSingle: Single<Unit>
        get() = syncAll()

    private fun syncAll(): Single<Unit> {
        return Single.just(Unit)
    }

    fun sync(coin: String, currency: String, chartType: ChartType): Single<Unit> {
        return statsProvider
                .getChartStats(coin, currency, chartType)
                .doOnSuccess { stats ->
                    storage.saveChartStats(stats)
                    listener?.onUpdate(stats, coin, currency, chartType)
                }
                .map { Unit }
    }

    private fun lastSyncTimestamp(): Long? {
        val activeKeys = subjectHolder.activeChartStatsKeys
        val chartStats = storage.getOldChartStats(activeKeys.map { it.chartType }, activeKeys.map { it.coin }, dataSource.currency)
        if (chartStats.size != activeKeys.size) {
            return null
        }

        return chartStats.lastOrNull()?.timestamp
    }

    private fun onFire() {
        for (subjectKey in subjectHolder.activeChartStatsKeys) {
            val latestStats = storage.getLatestChartStats(subjectKey.coin, subjectKey.currency, subjectKey.chartType)
            if (latestStats == null || subjectKey.chartType.isExpired(latestStats.timestamp)) {
                sync(subjectKey.coin, subjectKey.currency, subjectKey.chartType)
            }
        }
    }

    private fun onStop() {
        disposable?.dispose()
        disposable = null
    }
}
