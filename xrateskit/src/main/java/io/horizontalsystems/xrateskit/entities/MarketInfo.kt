package io.horizontalsystems.xrateskit.entities

import java.math.BigDecimal
import java.util.*

class MarketInfo(marketInfo: MarketInfoEntity, val expirationInterval: Long) {
    val currencyCode: String = marketInfo.currencyCode
    val rate: BigDecimal = marketInfo.rate
    val rateOpenDay: BigDecimal = marketInfo.rateOpenDay
    val rateDiff: BigDecimal = marketInfo.rateDiff
    val volume: BigDecimal = marketInfo.volume
    val marketCap: BigDecimal = marketInfo.marketCap
    val supply: BigDecimal = marketInfo.supply
    val liquidity: BigDecimal = marketInfo.liquidity
    val rateDiff1h: BigDecimal = marketInfo.rateDiff1h
    val rateDiff24h: BigDecimal = marketInfo.rateDiff24h
    val rateDiff7d: BigDecimal = marketInfo.rateDiff7d
    val rateDiff30d: BigDecimal = marketInfo.rateDiff30d
    val rateDiff1y: BigDecimal = marketInfo.rateDiff1y
    val timestamp: Long = marketInfo.timestamp

    fun isExpired(): Boolean {
        return Date().time / 1000 - expirationInterval > timestamp
    }
}
