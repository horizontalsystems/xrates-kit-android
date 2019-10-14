package io.horizontalsystems.xrateskit

import android.content.Context
import io.horizontalsystems.xrateskit.api.ApiManager
import io.horizontalsystems.xrateskit.api.CryptoCompareProvider
import io.horizontalsystems.xrateskit.core.DataProvider
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.SubjectHolder
import io.horizontalsystems.xrateskit.core.SyncScheduler
import io.horizontalsystems.xrateskit.entities.ChartPoint
import io.horizontalsystems.xrateskit.entities.ChartType
import io.horizontalsystems.xrateskit.entities.MarketStatsInfo
import io.horizontalsystems.xrateskit.entities.RateInfo
import io.horizontalsystems.xrateskit.managers.ChartStatsSyncer
import io.horizontalsystems.xrateskit.managers.HistoricalRateManager
import io.horizontalsystems.xrateskit.managers.LatestRateSyncer
import io.horizontalsystems.xrateskit.managers.MarketStatsManager
import io.horizontalsystems.xrateskit.storage.Database
import io.horizontalsystems.xrateskit.storage.Storage
import io.reactivex.Flowable
import io.reactivex.Single
import java.math.BigDecimal

class XRatesKit(
        private val dataSource: XRatesDataSource,
        private val subjectHolder: SubjectHolder,
        private val syncScheduler: SyncScheduler,
        private val dataProvider: DataProvider) {

    init {
        syncScheduler.start()
    }

    fun set(coins: List<String>) {
        dataSource.coins = coins
        subjectHolder.clear()

        syncScheduler.start()
    }

    fun set(currency: String) {
        dataSource.currency = currency
        syncScheduler.start()
    }

    fun refresh() {
        syncScheduler.start()
    }

    fun getHistoricalRate(coin: String, currency: String, timestamp: Long): Single<BigDecimal> {
        return dataProvider.getHistoricalRate(coin, currency, timestamp)
    }

    fun getLatestRate(coin: String, currency: String): RateInfo? {
        return dataProvider.getLatestRate(coin, currency)
    }

    fun getMarketStats(coin: String, currency: String): Single<MarketStatsInfo> {
        return dataProvider.getMarketStats(coin, currency)
    }

    fun getChartStats(coin: String, currency: String, chartType: ChartType): List<ChartPoint> {
        return dataProvider.getChartPoints(coin, currency, chartType)
    }

    fun latestRateFlowable(coin: String, currency: String): Flowable<RateInfo> {
        return subjectHolder.latestRateFlowable(coin, currency)
    }

    fun chartStatsFlowable(coin: String, currency: String, chartType: ChartType): Flowable<List<ChartPoint>> {
        return subjectHolder.chartStatsFlowable(coin, currency, chartType)
    }

    companion object {
        fun create(context: Context, currency: String): XRatesKit {
            val factory = Factory()
            val storage = Storage(Database.create(context))
            val dataSource = XRatesDataSource(currency = currency)

            val subjectHolder = SubjectHolder()
            val syncScheduler = SyncScheduler(5 * 60, 60)

            val apiManager = ApiManager()
            val cryptoCompareProvider = CryptoCompareProvider(factory, apiManager, "https://min-api.cryptocompare.com")

            val chartStatsSyncer = ChartStatsSyncer(storage, subjectHolder, cryptoCompareProvider)
            val latestRateSyncer = LatestRateSyncer(storage, dataSource, cryptoCompareProvider)

            val historicalRateManager = HistoricalRateManager(storage, cryptoCompareProvider)
            val marketStatsManager = MarketStatsManager(storage, cryptoCompareProvider)

            val dataProvider = DataProvider(storage, factory, subjectHolder, chartStatsSyncer, historicalRateManager, marketStatsManager)

            chartStatsSyncer.subscribe(syncScheduler)
            latestRateSyncer.subscribe(syncScheduler)
            latestRateSyncer.syncListener = syncScheduler
            latestRateSyncer.listener = dataProvider

            return XRatesKit(dataSource, subjectHolder, syncScheduler, dataProvider)
        }
    }
}
