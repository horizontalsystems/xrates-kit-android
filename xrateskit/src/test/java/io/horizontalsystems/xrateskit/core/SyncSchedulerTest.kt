package io.horizontalsystems.xrateskit.core

import io.horizontalsystems.xrateskit.rates.LatestRatesScheduler
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object SyncSchedulerTest : Spek({

    val syncScheduler by memoized {
        LatestRatesScheduler(5 * 60, 60)
    }

    describe("#start") {

    }

    describe("#stop") {
    }

    describe("#onSuccess") {
    }

    describe("#onFail") {
    }
})
