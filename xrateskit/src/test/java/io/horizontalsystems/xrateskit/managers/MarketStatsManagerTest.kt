package io.horizontalsystems.xrateskit.managers

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyZeroInteractions
import com.nhaarman.mockitokotlin2.whenever
import io.horizontalsystems.xrateskit.providers.cryptocompare.CryptoCompareProvider
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.MarketInfoEntity
import io.reactivex.Single
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.util.*

object MarketStatsManagerTest : Spek({
    val coin = "BTC"
    val currency = "USD"
    val marketStats by memoized { mock<MarketInfoEntity>() }

    val statsProvider by memoized { mock<CryptoCompareProvider>() }
    val storage by memoized { mock<IStorage>() }
    val marketStatsManager by memoized {
        MarketStatsManager(storage, statsProvider)
    }

    describe("#marketStatsSingle") {
        beforeEach {
            whenever(statsProvider.getMarketStats(coin, currency)).thenReturn(Single.just(marketStats))
        }

        context("when market stats not exists in DB") {
            beforeEach {
                whenever(storage.getLatestRate(coin, currency)).thenReturn(null)
            }

            it("fetches market stats from API") {
                marketStatsManager.getMarketStats(coin, currency)

                verify(statsProvider).getMarketStats(coin, currency)
            }
        }

        context("when market stats exists in DB and not yet expired") {
            beforeEach {
                whenever(marketStats.timestamp).thenReturn(Date().time / 1000)
                whenever(storage.getLatestRate(coin, currency)).thenReturn(marketStats)
            }

            it("returns market stats from DB") {
                marketStatsManager
                        .getMarketStats(coin, currency).test()
                        .assertResult(marketStats)

                verifyZeroInteractions(statsProvider)
            }
        }

        context("when market stats exists in DB and expired") {
            beforeEach {
                whenever(marketStats.timestamp).thenReturn(Date().time / 1000 - 24 * 60 * 60 - 1)
                whenever(storage.getLatestRate(coin, currency)).thenReturn(marketStats)
            }

            it("fetches market stats from API and saves it into DB") {
                marketStatsManager.getMarketStats(coin, currency).test().assertOf {
                    verify(statsProvider).getMarketStats(coin, currency)
                    verify(storage).saveLatestRates(marketStats)
                }
            }
        }
    }
})
