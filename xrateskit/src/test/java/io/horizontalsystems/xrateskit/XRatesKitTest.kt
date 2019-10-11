package io.horizontalsystems.xrateskit

import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.core.SyncScheduler
import io.horizontalsystems.xrateskit.managers.HistoricalRateManager
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

    describe("#refresh") {

        it("refreshes the kit") {
            ratesKit.refresh()
            verify(syncScheduler).start()
        }
    }

    describe("#set(coins)") {

        it("sets coins and restarts scheduler") {
            val newCoins = listOf("BCH", "DASH")
            ratesKit.set(newCoins)

            verify(dataSource).coins = newCoins
            verify(syncScheduler).start()
        }
    }

    describe("#set(currency)") {

        it("sets currency and restarts scheduler") {
            val newCurrency = "KGS"
            ratesKit.set(newCurrency)

            verify(syncScheduler).start()
            verify(dataSource).currency = newCurrency
        }
    }
})
