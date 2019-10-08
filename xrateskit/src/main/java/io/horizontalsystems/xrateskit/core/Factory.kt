package io.horizontalsystems.xrateskit.core

import io.horizontalsystems.xrateskit.storage.HistoricalRate
import io.horizontalsystems.xrateskit.storage.LatestRate
import io.horizontalsystems.xrateskit.storage.RateInfo
import java.math.BigDecimal
import java.util.*

class Factory {
    fun createLatestRate(coin: String, currency: String, value: BigDecimal): LatestRate {
        return LatestRate(coin, currency, value, timestamp = Date().time / 1000)
    }

    fun createHistoricalRate(coin: String, currency: String, value: BigDecimal, timestamp: Long): HistoricalRate {
        return HistoricalRate(coin, currency, value, timestamp)
    }

    fun createRateInfo(rate: LatestRate): RateInfo {
        return RateInfo(rate.coin, rate.currency, rate.value, rate.timestamp)
    }
}
