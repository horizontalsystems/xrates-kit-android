package io.horizontalsystems.xrateskit.core

import com.nhaarman.mockitokotlin2.*
import io.horizontalsystems.xrateskit.entities.*
import io.horizontalsystems.xrateskit.managers.ChartStatSyncer
import io.horizontalsystems.xrateskit.rates.HistoricalRateManager
import io.horizontalsystems.xrateskit.managers.MarketStatsManager
import org.junit.Assert.assertEquals
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.math.BigDecimal
import java.util.*

object DataProviderTest : Spek({
    val coin = "BTC"
    val currency = "USD"
    val chartType = ChartType.DAILY

    val factory by memoized<Factory> { mock() }
    val storage by memoized<IStorage> { mock() }
    val subjectHolder by memoized { SubjectHolder() }
    val chartStatsSyncer by memoized<ChartStatSyncer> { mock() }
    val historicalRateManager by memoized<HistoricalRateManager> { mock() }
    val marketStatsManager by memoized<MarketStatsManager> { mock() }

    val dataProvider by memoized {
        DataProvider(storage, factory, subjectHolder, chartStatsSyncer, historicalRateManager, marketStatsManager)
    }

    describe("#getChartInfo") {

        context("when chart stats does not exists in DB") {

            beforeEach {
                whenever(storage.getChartPoints(coin, currency, chartType)).thenReturn(listOf())
            }

            it("returns points from DB") {
                val points = dataProvider.getChartPoints(coin, currency, chartType)

                assertEquals(listOf<ChartPoint>(), points)
                verify(chartStatsSyncer).sync(coin, currency, chartType)
            }
        }

        context("when chart stats exists in DB but expired") {
            val today = Date().time / 1000
            val chartStats = ChartPointEntity(chartType, coin, currency, BigDecimal.TEN, today - (chartType.seconds + 1))

            beforeEach {
                whenever(storage.getChartPoints(coin, currency, chartType)).thenReturn(listOf(chartStats))
                whenever(factory.createChartPoint(chartStats.value, chartStats.timestamp))
                        .thenReturn(ChartPoint(chartStats.value, chartStats.timestamp))
            }

            it("returns points from DB and start syncer") {
                val points = dataProvider.getChartPoints(coin, currency, chartType)
                val pointsFromDB = listOf(ChartPoint(chartStats.value, chartStats.timestamp))

                assertEquals(pointsFromDB, points)
                verify(chartStatsSyncer).sync(coin, currency, chartType)
            }
        }

        context("when chart stats exists in DB") {
            val chartStats = ChartPointEntity(chartType, coin, currency, BigDecimal.TEN, Date().time / 1000 - 1)

            beforeEach {
                whenever(storage.getChartPoints(coin, currency, chartType)).thenReturn(listOf(chartStats))
                whenever(factory.createChartPoint(chartStats.value, chartStats.timestamp))
                        .thenReturn(ChartPoint(chartStats.value, chartStats.timestamp))
            }

            it("returns points from DB") {
                val points = dataProvider.getChartPoints(coin, currency, chartType)
                val pointsFromDB = listOf(ChartPoint(chartStats.value, chartStats.timestamp))

                assertEquals(pointsFromDB, points)
                verifyZeroInteractions(chartStatsSyncer)
            }

            context("when latest rate also exists in DB") {
                val latestRate = LatestRate(coin, currency, BigDecimal.TEN, Date().time / 1000)

                beforeEach {
                    whenever(storage.getLatestRate(coin, currency)).thenReturn(latestRate)
                    whenever(factory.createChartPoint(chartStats.value, chartStats.timestamp)).thenReturn(ChartPoint(chartStats.value, chartStats.timestamp))
                    whenever(factory.createChartPoint(latestRate.value, latestRate.timestamp)).thenReturn(ChartPoint(latestRate.value, latestRate.timestamp))
                }

                it("concatenates to to return list") {
                    val points = dataProvider.getChartPoints(coin, currency, chartType)
                    val pointsFromDB = listOf(
                            ChartPoint(chartStats.value, chartStats.timestamp),
                            ChartPoint(latestRate.value, latestRate.timestamp)
                    )

                    assertEquals(pointsFromDB, points)
                    verifyZeroInteractions(chartStatsSyncer)
                }
            }
        }
    }

    describe("#update(latestRate)") {
        val rateInfo by memoized<Rate> { mock() }
        val rateSubjectKey = MarketInfoKey(coin, currency)
        val latestRate by memoized<LatestRate> {
            mock {
                on { this.coin } doReturn coin
                on { this.currency } doReturn currency
            }
        }

        beforeEach {
            whenever(factory.createRateInfo(latestRate)).thenReturn(rateInfo)
        }

        it("emits latest rate update to active observers") {
            subjectHolder.latestRateSubject[rateSubjectKey] = mock()

            dataProvider.onUpdate(latestRate)

            verify(subjectHolder.latestRateSubject[rateSubjectKey])?.onNext(rateInfo)
        }
    }
})
