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

    fun createMarketInfoEntity(coin: String, currency: String, rate: BigDecimal, rateOpen24Hour: BigDecimal, rateDiff: BigDecimal, volume: BigDecimal, marketCap: BigDecimal, supply: BigDecimal): MarketInfoEntity {
        return MarketInfoEntity(coin, currency, rate, rateOpen24Hour, rateDiff, volume, supply, Date().time / 1000, BigDecimal.ZERO, marketCap)
    }

    fun createMarketInfoEntity(coin: Coin, marketInfo: MarketInfo): MarketInfoEntity {
        return MarketInfoEntity(  coinCode = coin.code,
                                  currencyCode = marketInfo.currencyCode,
                                  rate = marketInfo.rate,
                                  rateOpenDay = marketInfo.rateOpenDay,
                                  rateDiff = marketInfo.rateDiff,
                                  volume =  marketInfo.volume,
                                  supply = marketInfo.supply,
                                  timestamp = marketInfo.timestamp,
                                  rateDiffPeriod =  marketInfo.rateDiffPeriod,
                                  marketCap = marketInfo.marketCap,
                                  liquidity = marketInfo.liquidity)
    }

    fun createMarketInfo(marketInfoEntity: MarketInfoEntity): MarketInfo {
        return MarketInfo(marketInfoEntity, expirationInterval)
    }

    fun createCoinMarket(coin: Coin,
                         currency: String,
                         rate: BigDecimal,
                         rateOpenDay: BigDecimal,
                         rateDiff: BigDecimal,
                         volume: BigDecimal,
                         supply: BigDecimal,
                         rateDiffPeriod: BigDecimal = BigDecimal.ZERO,
                         marketCap: BigDecimal? = null,
                         liquidity: BigDecimal? = null): CoinMarket {
        val marketInfoEntity = MarketInfoEntity(coin.code, currency, rate, rateOpenDay, rateDiff, volume, supply,
                                                Date().time / 1000, rateDiffPeriod, marketCap, liquidity)
        val marketInfo = MarketInfo(marketInfoEntity, expirationInterval)
        return CoinMarket(coin, marketInfo)
    }

    fun createCryptoNews(id: Int, time: Long, imageUrl: String, title: String, url: String, body: String, types: List<String>): CryptoNews {
        return CryptoNews(id, time, imageUrl, title, url, body, types)
    }
}
