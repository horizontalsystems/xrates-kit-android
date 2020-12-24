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

    fun createMarketInfoEntity(coin: String, currency: String, rate: BigDecimal, rateOpen24Hour: BigDecimal, diff: BigDecimal, volume: BigDecimal, marketCap: BigDecimal, supply: BigDecimal): MarketInfoEntity {
        return MarketInfoEntity(coin, currency, rate, rateOpen24Hour, diff, volume, marketCap, supply, Date().time / 1000)
    }

    fun createMarketInfo(marketInfoEntity: MarketInfoEntity): MarketInfo {
        return MarketInfo(marketInfoEntity, expirationInterval)
    }

    fun createTopMarket(coin: Coin, marketInfoEntity: MarketInfoEntity): TopMarket {
        return TopMarket(coin, MarketInfo(marketInfoEntity, expirationInterval))
    }

    fun createTopMarket(topMarketCoin: TopMarketCoin, marketInfoEntity: MarketInfoEntity): TopMarket {
        return TopMarket(Coin(topMarketCoin.code, topMarketCoin.name), MarketInfo(marketInfoEntity, expirationInterval))
    }

    fun createTopMarket(coin: Coin,
                        currency: String,
                        rate: BigDecimal,
                        rateOpenDay: BigDecimal,
                        diff: BigDecimal,
                        volume: BigDecimal,
                        marketCap: BigDecimal,
                        supply: BigDecimal,
                        liquidity: BigDecimal = BigDecimal.ZERO,
                        rateDiff1h: BigDecimal = BigDecimal.ZERO,
                        rateDiff24h: BigDecimal = BigDecimal.ZERO,
                        rateDiff7d: BigDecimal = BigDecimal.ZERO,
                        rateDiff30d: BigDecimal = BigDecimal.ZERO,
                        rateDiff1y: BigDecimal = BigDecimal.ZERO): TopMarket {
        val marketInfoEntity = MarketInfoEntity(coin.code, currency, rate, rateOpenDay, diff, volume, marketCap, supply, Date().time / 1000,
                                                liquidity, rateDiff1h, rateDiff24h, rateDiff7d, rateDiff30d, rateDiff1y)
        val marketInfo = MarketInfo(marketInfoEntity, expirationInterval)
        return TopMarket(coin, marketInfo)
    }

    fun createCryptoNews(id: Int, time: Long, imageUrl: String, title: String, url: String, body: String, types: List<String>): CryptoNews {
        return CryptoNews(id, time, imageUrl, title, url, body, types)
    }
}
