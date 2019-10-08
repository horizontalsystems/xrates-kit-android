package io.horizontalsystems.xrateskit.core

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.horizontalsystems.xrateskit.XRatesDataSource
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

class LatestRateSyncerTest : Spek({
    val coins = listOf("BTC", "ETH")
    val currency = "USD"

    val dataSource = XRatesDataSource(coins, currency)
    val storage by memoized { mock<IStorage>() }
    val factory by memoized { mock<Factory>() }
    val rateProvider by memoized { mock<ILatestRateProvider>() }

    val latestRateSyncer by memoized {
        LatestRateSyncer(storage, factory, dataSource, rateProvider)
    }

    describe("#sync") {
        it("gets latest rate from provider") {
            latestRateSyncer.sync()
            verify(rateProvider).getLatestRate(coins, currency)
        }
    }

    describe("#onFire") {

    }

    describe("#onStop") {

    }

})
