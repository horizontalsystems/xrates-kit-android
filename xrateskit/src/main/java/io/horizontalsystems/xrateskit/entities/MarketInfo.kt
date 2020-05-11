package io.horizontalsystems.xrateskit.entities

import java.math.BigDecimal
import java.util.*

class MarketInfo(marketInfo: MarketInfoEntity, val expirationInterval: Long) {
    val currency: String = marketInfo.currency
    val rate: BigDecimal = marketInfo.rate
    val rateOpen24Hour: BigDecimal = marketInfo.rateOpen24Hour
    val diff: BigDecimal = marketInfo.diff
    val volume: Double = marketInfo.volume
    val marketCap: Double = marketInfo.marketCap
    val supply: Double = marketInfo.supply
    val timestamp: Long = marketInfo.timestamp

    fun isExpired(): Boolean {
        return Date().time / 1000 - expirationInterval > timestamp
    }
}
