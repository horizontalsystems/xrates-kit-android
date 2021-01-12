package io.horizontalsystems.xrateskit.entities

import java.math.BigDecimal
import java.util.*

class MarketInfo(marketInfo: MarketInfoEntity, val expirationInterval: Long) {
    val currencyCode: String = marketInfo.currencyCode
    val rate: BigDecimal = marketInfo.rate
    val rateOpenDay: BigDecimal = marketInfo.rateOpenDay
    val rateDiff: BigDecimal = marketInfo.rateDiff
    val volume: BigDecimal = marketInfo.volume
    val supply: BigDecimal = marketInfo.supply
    val rateDiffPeriod: BigDecimal = marketInfo.rateDiffPeriod
    val timestamp: Long = marketInfo.timestamp
    val liquidity: BigDecimal? = marketInfo.liquidity
    val marketCap: BigDecimal? = marketInfo.marketCap

    fun isExpired(): Boolean {
        return Date().time / 1000 - expirationInterval > timestamp
    }
}
