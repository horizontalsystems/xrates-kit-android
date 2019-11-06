package io.horizontalsystems.xrateskit.core

import io.horizontalsystems.xrateskit.entities.ChartPoint
import io.horizontalsystems.xrateskit.entities.HistoricalRate
import io.horizontalsystems.xrateskit.entities.MarketInfo
import io.horizontalsystems.xrateskit.entities.MarketInfoEntity
import java.math.BigDecimal
import java.util.*

class Factory(private val expirationInterval: Long) {
    fun createHistoricalRate(coin: String, currency: String, value: BigDecimal, timestamp: Long): HistoricalRate {
        return HistoricalRate(coin, currency, value, timestamp)
    }

    fun createChartPoint(value: BigDecimal, timestamp: Long): ChartPoint {
        return ChartPoint(value, timestamp)
    }

    fun createMarketInfoEntity(coin: String, currency: String, rate: BigDecimal, diff: BigDecimal, volume: Double, marketCap: Double, supply: Double): MarketInfoEntity {
        return MarketInfoEntity(coin, currency, rate, diff, volume, marketCap, supply, Date().time / 1000)
    }

    fun createMarketInfo(marketInfoEntity: MarketInfoEntity): MarketInfo {
        return MarketInfo(marketInfoEntity, expirationInterval)
    }
}
