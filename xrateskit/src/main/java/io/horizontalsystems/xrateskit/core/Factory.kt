package io.horizontalsystems.xrateskit.core

import io.horizontalsystems.xrateskit.entities.*
import java.math.BigDecimal
import java.util.*

class Factory(private val latestRateSyncInterval: Long) {
    fun createLatestRate(coin: String, currency: String, value: BigDecimal): LatestRate {
        return LatestRate(coin, currency, value, timestamp = Date().time / 1000)
    }

    fun createHistoricalRate(coin: String, currency: String, value: BigDecimal, timestamp: Long): HistoricalRate {
        return HistoricalRate(coin, currency, value, timestamp)
    }

    fun createRateInfo(rate: LatestRate): RateInfo {
        return RateInfo(rate.value, rate.timestamp, latestRateSyncInterval)
    }

    fun createChartPoint(value: BigDecimal, timestamp: Long): ChartPoint {
        return ChartPoint(value, timestamp)
    }

    fun createMarketCap(coin: String, currency: String, volume: Double, marketCap: Double, supply: Double): MarketStats {
        return MarketStats(coin, currency, volume, marketCap, supply, timestamp = Date().time / 1000)
    }

    fun createMarketCapInfo(marketStats: MarketStats): MarketStatsInfo {
        return MarketStatsInfo(marketStats.volume, marketStats.marketCap, marketStats.supply)
    }
}
