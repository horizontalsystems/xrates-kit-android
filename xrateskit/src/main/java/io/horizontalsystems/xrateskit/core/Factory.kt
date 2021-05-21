package io.horizontalsystems.xrateskit.core

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.entities.*
import java.math.BigDecimal
import java.util.*

class Factory(private val expirationInterval: Long) {
    fun createHistoricalRate(coinType: CoinType, currency: String, value: BigDecimal, timestamp: Long): HistoricalRate {
        return HistoricalRate(coinType, currency, value, timestamp)
    }

    fun createChartPoint(value: BigDecimal, volume: BigDecimal?, timestamp: Long): ChartPoint {
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

    fun createCoinMarket(coinData: CoinData, currency: String, rate: BigDecimal, rateOpenDay: BigDecimal,
        rateDiff: BigDecimal, volume: BigDecimal, supply: BigDecimal, rateDiffPeriod: BigDecimal?, marketCap: BigDecimal? = null,
                         dilutedMarketCap: BigDecimal? = null, totalSupply: BigDecimal? = null, athChangePercentage: BigDecimal? = null, atlChangePercentage: BigDecimal? = null
    ): CoinMarket {
        val marketInfo = MarketInfo(
            coinType = coinData.type,
            currencyCode = currency,
            rate = rate, rateOpenDay = rateOpenDay,
            rateDiff = rateDiff,
            volume = volume,
            supply = supply,
            rateDiffPeriod = rateDiffPeriod,
            timestamp =  Date().time / 1000,
            marketCap = marketCap,
            dilutedMarketCap = dilutedMarketCap,
            totalSupply = totalSupply,
            athChangePercentage = athChangePercentage,
            atlChangePercentage = atlChangePercentage,
            expirationInterval = expirationInterval
        )

        return CoinMarket(coinData, marketInfo)
    }

    fun createCryptoNews(id: Int, source: String, time: Long, imageUrl: String, title: String, url: String, body: String, types: List<String>): CryptoNews {
        return CryptoNews(id, source, time, imageUrl, title, url, body, types)
    }
}
