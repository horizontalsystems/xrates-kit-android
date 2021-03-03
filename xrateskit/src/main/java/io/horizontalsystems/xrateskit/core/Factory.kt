package io.horizontalsystems.xrateskit.core

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.entities.*
import java.math.BigDecimal
import java.util.*

class Factory(private val expirationInterval: Long) {
    fun createHistoricalRate(coinType: CoinType, currency: String, value: BigDecimal, timestamp: Long): HistoricalRate {
        return HistoricalRate(coinType, currency, value, timestamp)
    }

    fun createChartPoint(value: BigDecimal, volume: BigDecimal, timestamp: Long): ChartPoint {
        return ChartPoint(value, volume, timestamp)
    }

    fun createMarketInfoEntity(coinType: CoinType, currency: String, rate: BigDecimal, rateOpen24Hour: BigDecimal, rateDiff: BigDecimal, volume: BigDecimal, marketCap: BigDecimal, supply: BigDecimal): MarketInfoEntity {
        return MarketInfoEntity(coinType, currency, rate, rateOpen24Hour, rateDiff, volume, supply, Date().time / 1000, BigDecimal.ZERO, marketCap)
    }

    fun createMarketInfoEntity(coinType: CoinType, marketInfo: MarketInfo): MarketInfoEntity {
        return MarketInfoEntity(  coinType = coinType,
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

    fun createCoinMarket(coinData: CoinData,
                         currency: String,
                         rate: BigDecimal,
                         rateOpenDay: BigDecimal,
                         rateDiff: BigDecimal,
                         volume: BigDecimal,
                         supply: BigDecimal,
                         rateDiffPeriod: BigDecimal = BigDecimal.ZERO,
                         marketCap: BigDecimal? = null,
                         liquidity: BigDecimal? = null): CoinMarket {
        val marketInfoEntity = MarketInfoEntity(coinData.type, currency, rate, rateOpenDay, rateDiff, volume, supply,
                                                Date().time / 1000, rateDiffPeriod, marketCap, liquidity)
        val marketInfo = MarketInfo(marketInfoEntity, expirationInterval)
        return CoinMarket(coinData, marketInfo)
    }

    fun createCryptoNews(id: Int, time: Long, imageUrl: String, title: String, url: String, body: String, types: List<String>): CryptoNews {
        return CryptoNews(id, time, imageUrl, title, url, body, types)
    }
}
