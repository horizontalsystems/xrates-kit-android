package io.horizontalsystems.xrateskit.managers

import com.nhaarman.mockitokotlin2.*
import io.horizontalsystems.xrateskit.RxTestRule
import io.horizontalsystems.xrateskit.XRatesDataSource
import io.horizontalsystems.xrateskit.core.*
import io.horizontalsystems.xrateskit.entities.LatestRate
import io.horizontalsystems.xrateskit.rates.LatestRatesScheduler
import io.horizontalsystems.xrateskit.rates.LatestRateSyncer
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.subjects.PublishSubject
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class LatestRateSyncerTest : Spek({
    val coins = listOf("BTC", "ETH")
    val currency = "USD"
    val latestRate by memoized { mock<LatestRate>() }
    val dataSource by memoized { XRatesDataSource(currency = currency) }

    val storage by memoized { mock<IStorage>() }
    val rateProvider by memoized { mock<ILatestRateProvider>() }
    val rateListener by memoized { mock<LatestRateSyncer.Listener>() }
    val syncListener by memoized { mock<ISyncCompletionListener>() }

    val latestRateSyncer by memoized {
        LatestRateSyncer(storage, dataSource, rateProvider).also {
            it.listener = rateListener
            it.syncListener = syncListener
        }
    }

    beforeEachTest {
        RxTestRule.setup()
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
            latestRateSyncer.subscribe(scheduler)

            verify(scheduler.eventSubject).toFlowable(BackpressureStrategy.DROP)
            verify(flowable).subscribe(any<Consumer<SyncSchedulerEvent>>())
        }
    }

    describe("#sync") {
        context("when data source not provided") {
            beforeEach {
                dataSource.coins = listOf()
                dataSource.currency = ""
            }

            it("does't fetches latest rate from API") {
                latestRateSyncer.sync()
                verifyZeroInteractions(rateProvider)
            }
        }

        context("when data source set") {

            beforeEach {
                dataSource.coins = coins
                dataSource.currency = currency
            }

            it("fetches latest rate from API") {
                whenever(rateProvider.getLatestRatesAsync(coins, currency)).thenReturn(Observable.create {})
                latestRateSyncer.sync()

                verify(rateProvider).getLatestRatesAsync(coins, currency)
            }

            context("when latest rates fetched from API") {
                beforeEach {
                    whenever(rateProvider.getLatestRatesAsync(coins, currency)).thenReturn(Observable.just(latestRate))
                }

                it("saves fetched data into DB and emits update to listener") {
                    latestRateSyncer.sync()

                    verify(rateListener).onUpdate(latestRate)
                    verify(syncListener).onSuccess()
                }
            }

            context("when failed to fetch from API") {
                val stubException = Exception("Failed to fetch rate from API")

                beforeEach {
                    whenever(rateProvider.getLatestRatesAsync(coins, currency)).thenReturn(Observable.error(stubException))
                }

                it("emits `onFail` events to sync completion listener") {
                    latestRateSyncer.sync()

                    verifyZeroInteractions(storage)
                    verifyZeroInteractions(rateListener)
                    verify(syncListener).onFail()
                }
            }
        }
    }

})
