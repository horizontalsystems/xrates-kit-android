package io.horizontalsystems.xrateskit.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.horizontalsystems.coinkit.models.CoinType

data class CoinData(
    var type: CoinType,
    val code: String,
    val title: String
 )


class CoinMeta(
    val description: String,
    val links: Map<LinkType, String>,
    val rating: String?,
    var categories: List<CoinCategory>,
    val platforms: Map<CoinPlatformType, String>
)

enum class LinkType{
    GUIDE,
    WEBSITE,
    WHITEPAPER,
    TWITTER,
    TELEGRAM,
    REDDIT,
    GITHUB,
    YOUTUBE
}

enum class CoinPlatformType{
    ETHEREUM,
    BINANCE,
    BINANCE_SMART_CHAIN,
    TRON,
    EOS,
}

@Entity
data class CoinInfoEntity(
    @PrimaryKey
    val coinType: CoinType,
    val code: String,
    val name: String,
    val rating: String?,
    val description: String?)

@Entity(primaryKeys = ["id"])
data class CoinCategory(val id: String, val name: String)

@Entity(primaryKeys = ["coinType", "linkType"])
data class CoinLinksEntity(
    val coinType: CoinType,
    val linkType: LinkType,
    val link: String)

@Entity(primaryKeys = ["coinType", "categoryId"])
data class CoinCategoriesEntity(val coinType: CoinType, val categoryId: String)

@Entity
data class ProviderCoinEntity(
    @PrimaryKey
    val coinType: CoinType,
    val code: String,
    val name: String,
    val coingeckoId: String?,
    val cryptocompareId: String?
)

