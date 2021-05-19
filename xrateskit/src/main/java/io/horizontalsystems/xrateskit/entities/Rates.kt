package io.horizontalsystems.xrateskit.entities

import androidx.room.Entity
import io.horizontalsystems.coinkit.models.CoinType
import java.math.BigDecimal
import java.util.*

data class LatestRate(val latestRateEntity: LatestRateEntity, val expirationInterval: Long){
        val currencyCode = latestRateEntity.currencyCode
        val rate = latestRateEntity.rate
        val rateDiff24h = latestRateEntity.rateDiff24h
        val timestamp= latestRateEntity.timestamp

        fun isExpired(): Boolean {
                return Date().time / 1000 - expirationInterval > timestamp
        }
}

@Entity(primaryKeys = ["coinType", "currencyCode"])
data class LatestRateEntity(
        var coinType: CoinType,
        val currencyCode: String,
        val rate: BigDecimal,
        val rateDiff24h: BigDecimal?,
        val timestamp: Long
)

data class LatestRateKey(
        val coinTypes: List<CoinType>,
        val currencyCode: String
)

data class PairKey(
        val coinType: CoinType,
        val currencyCode: String
)

@Entity(primaryKeys = ["coinType", "currencyCode", "timestamp"])
class HistoricalRate(
        val coinType: CoinType,
        val currencyCode: String,
        val value: BigDecimal,
        val timestamp: Long
)
