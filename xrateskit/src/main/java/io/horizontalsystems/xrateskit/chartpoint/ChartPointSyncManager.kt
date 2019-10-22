package io.horizontalsystems.xrateskit.chartpoint

import io.horizontalsystems.xrateskit.entities.ChartInfo
import io.horizontalsystems.xrateskit.entities.ChartPointKey
import io.horizontalsystems.xrateskit.entities.LatestRateKey
import io.horizontalsystems.xrateskit.latestrate.LatestRateSyncManager
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.atomic.AtomicInteger

class ChartPointSyncManager(
        private val factory: ChartPointSchedulerFactory,
        private val chartPointManager: ChartPointManager,
        private val latestRateSyncManager: LatestRateSyncManager)
    : ChartPointManager.Listener {

    private val subjects = mutableMapOf<ChartPointKey, PublishSubject<ChartInfo>>()
    private val schedulers = mutableMapOf<ChartPointKey, ChartPointScheduler>()
    private val ratesDisposables = mutableMapOf<ChartPointKey, Disposable>()
    private val observersCount = mutableMapOf<ChartPointKey, AtomicInteger>()

    fun chartPointsObservable(key: ChartPointKey): Observable<ChartInfo> {
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

    //  ChartPointManager Listener

    override fun onUpdate(chartInfo: ChartInfo?, key: ChartPointKey) {
        if (chartInfo != null)
            subjects[key]?.onNext(chartInfo)
    }

    private fun getSubject(key: ChartPointKey): Observable<ChartInfo> {
        var subject = subjects[key]
        if (subject == null) {
            subject = PublishSubject.create<ChartInfo>()
            subjects[key] = subject
        }

        return subject
    }

    private fun getScheduler(key: ChartPointKey): ChartPointScheduler {
        var scheduler = schedulers[key]
        if (scheduler == null) {
            scheduler = factory.getScheduler(key)
            schedulers[key] = scheduler
        }


        return scheduler
    }

    private fun observeLatestRates(key: ChartPointKey) {
        latestRateSyncManager.latestRateObservable(LatestRateKey(key.coin, key.currency))
                .subscribeOn(Schedulers.io())
                .subscribe({
                    chartPointManager.update(it, key)
                }, {})
                .let {
                    ratesDisposables[key] = it
                }
    }

    private fun cleanup(key: ChartPointKey) {
        val subject = subjects[key]
        if (subject == null || getCounter(key).get() > 0) {
            return
        }

        subject.onComplete()
        schedulers[key]?.stop()
        ratesDisposables[key]?.dispose()
    }

    @Synchronized
    private fun getCounter(key: ChartPointKey): AtomicInteger {
        var count = observersCount[key]
        if (count == null) {
            count = AtomicInteger(0)
            observersCount[key] = count
        }

        return count
    }
}
