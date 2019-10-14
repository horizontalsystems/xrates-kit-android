package io.horizontalsystems.xrateskit.core

import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.subjects.PublishSubject

class SubjectHolder {
    val latestRateSubject = mutableMapOf<LatestRateSubjectKey, PublishSubject<RateInfo>>()
    val chartStatsSubject = mutableMapOf<ChartStatsSubjectKey, PublishSubject<List<ChartPoint>>>()

    val activeChartStatsKeys: List<ChartStatsSubjectKey>
        get() {
            val activeKeys = mutableListOf<ChartStatsSubjectKey>()

            chartStatsSubject.map { (key, value) ->
                if (value.hasObservers()) {
                    activeKeys.add(key)
                }
            }

            return activeKeys
        }

    fun latestRateFlowable(coin: String, currency: String): Flowable<RateInfo> {
        val subjectKey = LatestRateSubjectKey(coin, currency)

        var subject = latestRateSubject[subjectKey]
        if (subject == null) {
            subject = PublishSubject.create<RateInfo>()
            latestRateSubject[subjectKey] = subject
        }

        return subject.toFlowable(BackpressureStrategy.BUFFER)
    }

    fun chartStatsFlowable(coin: String, currency: String, chartType: ChartType): Flowable<List<ChartPoint>> {
        val subjectKey = ChartStatsSubjectKey(coin, currency, chartType)

        var subject = chartStatsSubject[subjectKey]
        if (subject == null) {
            subject = PublishSubject.create<List<ChartPoint>>()
            chartStatsSubject[subjectKey] = subject
        }

        return subject.toFlowable(BackpressureStrategy.BUFFER)
    }

    fun clear() {
        latestRateSubject.clear()
        chartStatsSubject.clear()
    }
}
