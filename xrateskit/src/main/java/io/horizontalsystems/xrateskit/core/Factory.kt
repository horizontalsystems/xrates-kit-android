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

    fun createLatestRateEntity(coinType: CoinType, marketInfo: MarketInfo): LatestRateEntity {
        return LatestRateEntity(
            coinType = coinType,
            currencyCode = marketInfo.currencyCode,
            rate = marketInfo.rate,
            rateDiff24h = marketInfo.rateDiffPeriod,
            timestamp = marketInfo.timestamp
        )
    }

    fun createLatestRate(latestRateEntity: LatestRateEntity): LatestRate {
        return LatestRate(latestRateEntity, expirationInterval)
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
