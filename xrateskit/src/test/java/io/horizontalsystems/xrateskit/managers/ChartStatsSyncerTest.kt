package io.horizontalsystems.xrateskit.managers

import com.nhaarman.mockitokotlin2.*
import io.horizontalsystems.xrateskit.core.*
import io.horizontalsystems.xrateskit.entities.ChartStats
import io.horizontalsystems.xrateskit.entities.ChartStatsSubjectKey
import io.horizontalsystems.xrateskit.entities.ChartType
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.math.BigDecimal
import java.util.*

object ChartStatsSyncerTest : Spek({
    val coin = "BTC"
    val currency = "USD"
    val chartType = ChartType.DAILY

    val storage by memoized { mock<IStorage>() }
    val subjectHolder by memoized { mock<SubjectHolder>() }
    val statsProvider by memoized { mock<IChartStatsProvider>() }
    val statsListener by memoized { mock<ChartStatsSyncer.Listener>() }

    val syncListener by memoized { mock<ISyncCompletionListener>() }

    val chartStatsSyncer by memoized {
        ChartStatsSyncer(storage, subjectHolder, statsProvider).also {
            it.listener = statsListener
            it.syncListener = syncListener
        }
    }

    describe("#sync") {
        context("when latest rates fetched from API") {
            val stats = listOf<ChartStats>(mock())

            beforeEach {
                whenever(statsProvider.getChartStats(coin, currency, chartType)).thenReturn(Single.just(stats))
            }

            it("saves fetched data into DB and emits update to listener") {
                chartStatsSyncer.sync(coin, currency, chartType)

                verify(storage).saveChartStats(stats)
                verify(statsListener).onUpdate(stats, coin, currency, chartType)
                verify(syncListener).onSuccess()
            }
        }

        context("when failed to fetch from API") {
            val stubException = Exception("Failed to fetch rate from API")

            beforeEach {
                whenever(statsProvider.getChartStats(coin, currency, chartType)).thenReturn(Single.error(stubException))
            }

            it("emits `onFail` events to sync completion listener") {
                chartStatsSyncer.sync(coin, currency, chartType)

                verifyZeroInteractions(storage)
                verify(syncListener).onFail()
            }
        }
    }

    describe("#subscribe") {

        val disposable by memoized { mock<Disposable>() }
        val flowable by memoized { mock<Flowable<SyncSchedulerEvent>>() }
        val subject by memoized { mock<PublishSubject<SyncSchedulerEvent>>() }
        val scheduler by memoized { mock<SyncScheduler>() }

        beforeEach {
            whenever(flowable.subscribe(any<Consumer<SyncSchedulerEvent>>())).thenReturn(disposable)
            whenever(subject.toFlowable(any())).thenReturn(flowable)
            whenever(scheduler.eventSubject).thenReturn(subject)
        }

        it("subscribes to event subjects of scheduler") {
            chartStatsSyncer.subscribe(scheduler)

            verify(scheduler.eventSubject).toFlowable(BackpressureStrategy.DROP)
            verify(flowable).subscribe(any<Consumer<SyncSchedulerEvent>>())
        }
    }

    describe("#subscribe(onFire)") {
        val subject = PublishSubject.create<SyncSchedulerEvent>()
        val scheduler by memoized { mock<SyncScheduler>() }

        beforeEach {
            whenever(scheduler.eventSubject).thenReturn(subject)
            chartStatsSyncer.subscribe(scheduler)
        }

        context("when subject holders has no observers") {
            beforeEach {
                whenever(subjectHolder.activeChartStatsKeys).thenReturn(listOf())
            }

            it("does nothing (skips checking from DB and fetching from API)") {
                subject.onNext(SyncSchedulerEvent.FIRE)

                verifyZeroInteractions(storage)
                verifyZeroInteractions(statsProvider)
            }
        }

        context("when subject holders has observers") {
            val subjectKey = ChartStatsSubjectKey(coin, currency, chartType)

            beforeEach {
                whenever(subjectHolder.activeChartStatsKeys).thenReturn(listOf(subjectKey))
                whenever(statsProvider.getChartStats(coin, currency, chartType)).thenReturn(Single.just(listOf()))
            }

            context("when no chart stats in DB") {
                beforeEach {
                    whenever(storage.getLatestChartStats(coin, currency, chartType)).thenReturn(null)
                }

                it("syncs from the beginning") {
                    subject.onNext(SyncSchedulerEvent.FIRE)
                    verify(statsProvider).getChartStats(coin, currency, chartType)
                }
            }

            context("when chart stats exists in DB but expired") {

                beforeEach {
                    val today = Date().time / 1000
                    val chartStats = ChartStats(chartType, coin, currency, BigDecimal.TEN, today - (chartType.minutes * 60 + 1))
                    whenever(storage.getLatestChartStats(coin, currency, chartType)).thenReturn(chartStats)
                }

                it("syncs from the beginning") {
                    subject.onNext(SyncSchedulerEvent.FIRE)
                    verify(statsProvider).getChartStats(coin, currency, chartType)
                }
            }

            context("when chart stats exists in DB but not expired yet") {

                beforeEach {
                    val today = Date().time / 1000
                    val chartStats = ChartStats(chartType, coin, currency, BigDecimal.TEN, today - (chartType.minutes * 60))
                    whenever(storage.getLatestChartStats(coin, currency, chartType)).thenReturn(chartStats)
                }

                it("syncs from the beginning") {
                    subject.onNext(SyncSchedulerEvent.FIRE)

                    verifyZeroInteractions(statsProvider)
                }
            }

        }
    }
})