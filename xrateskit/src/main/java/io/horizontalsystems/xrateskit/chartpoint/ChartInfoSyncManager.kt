package io.horizontalsystems.xrateskit.chartpoint

import io.horizontalsystems.xrateskit.core.NoChartInfo
import io.horizontalsystems.xrateskit.entities.ChartInfo
import io.horizontalsystems.xrateskit.entities.ChartInfoKey
import io.horizontalsystems.xrateskit.entities.MarketInfoKey
import io.horizontalsystems.xrateskit.marketinfo.MarketInfoSyncManager
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.atomic.AtomicInteger

class ChartInfoSyncManager(
        private val factory: ChartInfoSchedulerFactory,
        private val chartPointManager: ChartInfoManager,
        private val latestRateSyncManager: MarketInfoSyncManager)
    : ChartInfoManager.Listener {

    private val subjects = mutableMapOf<ChartInfoKey, PublishSubject<ChartInfo>>()
    private val schedulers = mutableMapOf<ChartInfoKey, ChartInfoScheduler>()
    private val ratesDisposables = mutableMapOf<ChartInfoKey, Disposable>()
    private val observersCount = mutableMapOf<ChartInfoKey, AtomicInteger>()
    private val failedKeys = mutableListOf<ChartInfoKey>()

    fun chartInfoObservable(key: ChartInfoKey): Observable<ChartInfo> {
        if (failedKeys.contains(key)) {
            return Observable.error(NoChartInfo())
        }

        return getSubject(key)
                .doOnSubscribe {
                    getCounter(key).incrementAndGet()
                    getScheduler(key).start()
                }
                .doOnDispose {
                    getCounter(key).decrementAndGet()
                    cleanup(key)
                }
    }

    //  ChartInfoManager Listener

    override fun onUpdate(chartInfo: ChartInfo, key: ChartInfoKey) {
        subjects[key]?.onNext(chartInfo)
    }

    override fun noChartInfo(key: ChartInfoKey) {
        failedKeys.add(key)
        subjects[key]?.onError(NoChartInfo())
    }

    private fun getSubject(key: ChartInfoKey): Observable<ChartInfo> {
        var subject = subjects[key]
        if (subject == null) {
            subject = PublishSubject.create<ChartInfo>()
            subjects[key] = subject
        }

        return subject
    }

    private fun getScheduler(key: ChartInfoKey): ChartInfoScheduler {
        var scheduler = schedulers[key]
        if (scheduler == null) {
            scheduler = factory.getScheduler(key)
            schedulers[key] = scheduler
        }

        observeLatestRates(key)

        return scheduler
    }

    private fun observeLatestRates(key: ChartInfoKey) {
        latestRateSyncManager.marketInfoObservable(MarketInfoKey(key.coin, key.currency))
                .subscribeOn(Schedulers.io())
                .subscribe({
                    chartPointManager.update(it, key)
                }, {})
                .let {
                    ratesDisposables[key] = it
                }
    }

    private fun cleanup(key: ChartInfoKey) {
        val subject = subjects[key]
        if (subject == null || getCounter(key).get() > 0) {
            return
        }

        subject.onComplete()
        schedulers[key]?.stop()
        ratesDisposables[key]?.dispose()
    }

    @Synchronized
    private fun getCounter(key: ChartInfoKey): AtomicInteger {
        var count = observersCount[key]
        if (count == null) {
            count = AtomicInteger(0)
            observersCount[key] = count
        }

        return count
    }
}
