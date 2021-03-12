package io.horizontalsystems.xrateskit.managers

import com.nhaarman.mockitokotlin2.*
import io.horizontalsystems.xrateskit.core.*
import io.horizontalsystems.xrateskit.entities.ChartPointEntity
import io.horizontalsystems.xrateskit.entities.ChartInfoKey
import io.horizontalsystems.xrateskit.entities.ChartType
import io.horizontalsystems.xrateskit.rates.LatestRatesScheduler
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
    val statsProvider by memoized { mock<IChartInfoProvider>() }
    val statsListener by memoized { mock<ChartStatSyncer.Listener>() }

    val syncListener by memoized { mock<ISyncCompletionListener>() }

    val chartStatsSyncer by memoized {
        ChartStatSyncer(storage, subjectHolder, statsProvider).also {
            it.listener = statsListener
            it.syncListener = syncListener
        }
    }

    describe("#sync") {
        context("when latest rates fetched from API") {
            val stats = listOf<ChartPointEntity>(mock())

            beforeEach {
                whenever(statsProvider.getChartPointsAsync(coin, currency, chartType)).thenReturn(Single.just(stats))
            }

            it("saves fetched data into DB and emits update to listener") {
                chartStatsSyncer.sync(coin, currency, chartType)

                verify(storage).saveChartPoints(stats)
                verify(statsListener).onUpdate(stats, coin, currency, chartType)
                verify(syncListener).onSuccess()
            }
        }

        context("when failed to fetch from API") {
            val stubException = Exception("Failed to fetch rate from API")

            beforeEach {
                whenever(statsProvider.getChartPointsAsync(coin, currency, chartType)).thenReturn(Single.error(stubException))
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
        val scheduler by memoized { mock<LatestRatesScheduler>() }

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
        val scheduler by memoized { mock<LatestRatesScheduler>() }

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
            val subjectKey = ChartInfoKey(coin, currency, chartType)

            beforeEach {
                whenever(subjectHolder.activeChartStatsKeys).thenReturn(listOf(subjectKey))
                whenever(statsProvider.getChartPointsAsync(coin, currency, chartType)).thenReturn(Single.just(listOf()))
            }

            context("when no chart stats in DB") {
                beforeEach {
                    whenever(storage.getLatestChartPoints(coin, currency, chartType)).thenReturn(null)
                }

                it("syncs from the beginning") {
                    subject.onNext(SyncSchedulerEvent.FIRE)
                    verify(statsProvider).getChartPointsAsync(coin, currency, chartType)
                }
            }

            context("when chart stats exists in DB but expired") {

                beforeEach {
                    val today = Date().time / 1000
                    val chartStats = ChartPointEntity(chartType, coin, currency, BigDecimal.TEN, today - (chartType.seconds + 1))
                    whenever(storage.getLatestChartPoints(coin, currency, chartType)).thenReturn(chartStats)
                }

                it("syncs from the beginning") {
                    subject.onNext(SyncSchedulerEvent.FIRE)
                    verify(statsProvider).getChartPointsAsync(coin, currency, chartType)
                }
            }

            context("when chart stats exists in DB but not expired yet") {

                beforeEach {
                    val today = Date().time / 1000
                    val chartStats = ChartPointEntity(chartType, coin, currency, BigDecimal.TEN, today - (chartType.seconds))
                    whenever(storage.getLatestChartPoints(coin, currency, chartType)).thenReturn(chartStats)
                }

                it("syncs from the beginning") {
                    subject.onNext(SyncSchedulerEvent.FIRE)

                    verifyZeroInteractions(statsProvider)
                }
            }

        }
    }
})
