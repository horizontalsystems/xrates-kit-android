package io.horizontalsystems.xrateskit.chartpoint

import io.horizontalsystems.xrateskit.core.NoChartInfo
import io.horizontalsystems.xrateskit.entities.ChartInfo
import io.horizontalsystems.xrateskit.entities.ChartInfoKey
import io.horizontalsystems.xrateskit.scheduler.Scheduler
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

class ChartInfoSyncManager(private val factory: ChartInfoSchedulerFactory)
    : ChartInfoManager.Listener {

    private val subjects = ConcurrentHashMap<ChartInfoKey, PublishSubject<ChartInfo>>()
    private val schedulers = ConcurrentHashMap<ChartInfoKey, Scheduler>()
    private val observers = ConcurrentHashMap<ChartInfoKey, AtomicInteger>()

    private val failedKeys = ConcurrentLinkedQueue<ChartInfoKey>()
    private val disposables = ConcurrentHashMap<ChartInfoKey, Disposable>()

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
                .doOnError {
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
        if (subjects[key]?.hasObservers() == true) {
            subjects[key]?.onError(NoChartInfo())
        }
    }

    @Synchronized
    private fun getSubject(key: ChartInfoKey): Observable<ChartInfo> {
        var subject = subjects[key]
        if (subject == null) {
            subject = PublishSubject.create<ChartInfo>()
            subjects[key] = subject
        }

        return subject
    }

    @Synchronized
    private fun getScheduler(key: ChartInfoKey): Scheduler {
        var scheduler = schedulers[key]
        if (scheduler == null) {
            scheduler = factory.getScheduler(key)
            schedulers[key] = scheduler
        }

        return scheduler
    }

    private fun cleanup(key: ChartInfoKey) {
        val subject = subjects[key]
        if (subject == null || getCounter(key).get() > 0) {
            return
        }

        subject.onComplete()
        subjects.remove(key)

        schedulers[key]?.stop()
        schedulers.remove(key)

        disposables[key]?.dispose()
    }

    private fun getCounter(key: ChartInfoKey): AtomicInteger {
        var count = observers[key]
        if (count == null) {
            count = AtomicInteger(0)
            observers[key] = count
        }

        return count
    }
}
