package io.horizontalsystems.xrateskit.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import io.horizontalsystems.coinkit.models.CoinType
import java.sql.Timestamp
import java.util.*

data class CoinData(
    var type: CoinType,
    val code: String,
    val title: String
 )


class CoinMeta(
    val description: String,
    val descriptionType: DescriptionType,
    val links: Map<LinkType, String>,
    val rating: String?,
    var categories: List<CoinCategory>,
    var fundCategories: List<CoinFundCategory>,
    val platforms: Map<CoinPlatformType, String>,
    val launchDate: Date? = null,
    val securityParameter: SecurityParameter? = null
) {
    enum class DescriptionType {
        HTML, MARKDOWN
    }
}

enum class Level {
    LOW,
    MEDIUM,
    HIGH;

    companion object {
        fun intValue(level: Level): Int{
            return when(level){
                LOW -> 1
                MEDIUM -> 2
                HIGH -> 3
            }
        }

        fun fromInt(intValue: Int): Level {
            return when(intValue){
                1 -> LOW
                2 -> MEDIUM
                else -> HIGH
            }
        }
    }
}

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
    OTHER,
    ETHEREUM,
    BINANCE,
    BINANCE_SMART_CHAIN,
    TRON,
    EOS,
}

@Entity
data class SecurityParameter(
    @PrimaryKey
    val coinType: CoinType,
    val privacy: Level,
    val decentralized: Boolean,
    val confiscationResistance: Boolean,
    val censorshipResistance: Boolean
)

@Entity
data class ExchangeInfoEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val imageUrl: String)

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

@Entity(indices = [Index(value = ["code"]), Index(value = ["name"]), Index(value = ["priority"])])
data class ProviderCoinEntity(
    @PrimaryKey
    val coinType: CoinType,
    val code: String,
    val name: String,
    val coingeckoId: String?,
    val cryptocompareId: String?,
    val priority: Int = Int.MAX_VALUE
)

@Entity(primaryKeys = ["id"])
data class CoinFund(
    val id: String,
    val name: String,
    val url: String,
    val categoryId: String)

@Entity(primaryKeys = ["coinType", "fundId"])
data class CoinFundsEntity(val coinType: CoinType, val fundId: String)

@Entity
data class CoinFundCategory(
    @PrimaryKey
    val id: String,
    val name: String,
    val order: Int){

    @Ignore
    var funds = mutableListOf<CoinFund>()
}

@Entity
data class Auditor(
    @PrimaryKey
    val id :String,
    val name :String,
) {
    @Ignore
    val reports: MutableList<AuditReport> = mutableListOf()
}

@Entity(indices = [Index(value = ["name", "timestamp"], unique = true)])
data class AuditReport(
    @PrimaryKey(autoGenerate = true)
    val id :Long = 0,
    val name :String,
    val timestamp: Long,
    val issues: Int = 0,
    val link: String?
)

@Entity(primaryKeys = ["auditorId", "coinType","reportId"])
data class CoinAuditReports(val coinType: CoinType, val auditorId: String, val reportId: Long)
