package io.horizontalsystems.xrateskit.chartpoint

import io.horizontalsystems.xrateskit.entities.ChartPointInfo
import io.horizontalsystems.xrateskit.entities.ChartPointKey
import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import java.util.concurrent.atomic.AtomicInteger

class ChartPointSyncManager(private val factory: ChartPointSchedulerFactory)
    : ChartPointManager.Listener {

    private val subjects = mutableMapOf<ChartPointKey, PublishSubject<List<ChartPointInfo>>>()
    private val schedulers = mutableMapOf<ChartPointKey, ChartPointScheduler>()
    private val observersCount = mutableMapOf<ChartPointKey, AtomicInteger>()

    fun chartPointsObservable(key: ChartPointKey): Observable<List<ChartPointInfo>> {
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

    // ChartPointManager Listener

    override fun onUpdate(points: List<ChartPointInfo>, key: ChartPointKey) {
        subjects[key]?.onNext(points)
    }

    private fun getSubject(key: ChartPointKey): Observable<List<ChartPointInfo>> {
        var subject = subjects[key]
        if (subject == null) {
            subject = PublishSubject.create<List<ChartPointInfo>>()
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

    private fun cleanup(key: ChartPointKey) {
        val subject = subjects[key]
        if (subject == null || getCounter(key).get() > 0) {
            return
        }

        subject.onComplete()
        schedulers[key]?.stop()
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
