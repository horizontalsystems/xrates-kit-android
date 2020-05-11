package io.horizontalsystems.xrateskit.core

import io.horizontalsystems.xrateskit.entities.*
import java.math.BigDecimal
import java.util.*

class Factory(private val expirationInterval: Long) {
    fun createHistoricalRate(coin: String, currency: String, value: BigDecimal, timestamp: Long): HistoricalRate {
        return HistoricalRate(coin, currency, value, timestamp)
    }

    fun createChartPoint(value: BigDecimal, volume: BigDecimal, timestamp: Long): ChartPoint {
        return ChartPoint(value, volume, timestamp)
    }

    fun createMarketInfoEntity(coin: String, currency: String, rate: BigDecimal, rateOpen24Hour: BigDecimal, diff: BigDecimal, volume: Double, marketCap: Double, supply: Double): MarketInfoEntity {
        return MarketInfoEntity(coin, currency, rate, rateOpen24Hour, diff, volume, marketCap, supply, Date().time / 1000)
    }

    fun createMarketInfo(marketInfoEntity: MarketInfoEntity): MarketInfo {
        return MarketInfo(marketInfoEntity, expirationInterval)
    }

    fun createTopMarket(topMarketCoin: TopMarketCoin, marketInfoEntity: MarketInfoEntity): TopMarket {
        return TopMarket(topMarketCoin.code, topMarketCoin.name, MarketInfo(marketInfoEntity, expirationInterval))
    }

    fun createTopMarket(coinCode: String, coinName: String, currency: String, rate: BigDecimal, rateOpen24Hour: BigDecimal, diff: BigDecimal, volume: Double, marketCap: Double, supply: Double): TopMarket {
        val marketInfoEntity = MarketInfoEntity(coinCode, currency, rate, rateOpen24Hour, diff, volume, marketCap, supply, Date().time / 1000)
        val marketInfo = MarketInfo(marketInfoEntity, expirationInterval)
        return TopMarket(coinCode, coinName, marketInfo)
    }

    fun createCryptoNews(id: Int, time: Long, imageUrl: String, title: String, url: String, body: String, types: List<String>): CryptoNews {
        return CryptoNews(id, time, imageUrl, title, url, body, types)
    }
}
