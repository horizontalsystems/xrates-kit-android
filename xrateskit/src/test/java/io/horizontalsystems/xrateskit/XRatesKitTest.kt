package io.horizontalsystems.xrateskit

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.horizontalsystems.xrateskit.core.HistoricalRateManager
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.core.SyncScheduler
import org.junit.Assert.assertEquals
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object XRatesKitTest : Spek({

    val coins = listOf("BTC", "ETH")
    val currency = "KGS"

    val syncScheduler by memoized { mock<SyncScheduler>() }
    val historicalRateManager by memoized { mock<HistoricalRateManager>() }
    val storage by memoized { mock<IStorage>() }
    val dataSource by memoized {
        mock<XRatesDataSource> {
            on { this.coins } doReturn listOf()
            on { this.currency } doReturn "USD"
        }
    }

    val ratesKit by memoized { XRatesKit(storage, dataSource, syncScheduler, historicalRateManager) }

    describe("#start") {

        it("starts the kit") {
            assertEquals(listOf<String>(), dataSource.coins)
            assertEquals("USD", dataSource.currency)

            ratesKit.start(coins, currency)

            verify(dataSource).coins = coins
            verify(dataSource).currency = currency
            verify(syncScheduler).start()
        }
    }

    describe("#refresh") {

        it("refreshes the kit") {
            ratesKit.refresh()
            verify(syncScheduler).start()
        }
    }

    describe("#stop") {

        it("stops the kit") {
            ratesKit.stop()
            verify(syncScheduler).stop()
        }
    }
})
