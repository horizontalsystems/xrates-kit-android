package io.horizontalsystems.xrateskit.entities

import androidx.room.Entity
import java.math.BigDecimal

enum class LinkType{
    GUIDE,
    WEBSITE,
    WHITEPAPER,
    TWITTER,
    TELEGRAM,
    REDDIT,
    GITHUB
}

enum class CoinPlatformType{
    ETHEREUM,
    BINANCE,
    BINANCE_SMART_CHAIN,
    TRON,
    EOS,
}

@Entity(primaryKeys = ["id"])
data class CoinInfoEntity(
    val id: String,
    val code: String,
    val name: String,
    val rating: String,
    val description: String)

@Entity(primaryKeys = ["id"])
data class CoinCategory(val id: String, val name: String)

@Entity(primaryKeys = ["coinId", "categoryId"])
data class CoinCategoriesEntity(val coinId: String, val categoryId: String)

class CoinMarketDetails(
    val coin: Coin,
    val currencyCode: String,

    val rate: BigDecimal,
    val rateHigh24h: BigDecimal,
    val rateLow24h: BigDecimal,

    val totalSupply: BigDecimal,
    val circulatingSupply: BigDecimal,

    val volume24h: BigDecimal,

    val marketCap: BigDecimal,
    val marketCapDiff24h: BigDecimal,

    val coinInfo: CoinInfo,
    val rateDiffs: Map<TimePeriod, Map<String, BigDecimal> >
)

class CoinInfo(
    val description: String,
    val links: Map<LinkType, String>,
    val rating: String?,
    var categories: List<CoinCategory>?,
    val platforms: Map<CoinPlatformType, String>?
)
