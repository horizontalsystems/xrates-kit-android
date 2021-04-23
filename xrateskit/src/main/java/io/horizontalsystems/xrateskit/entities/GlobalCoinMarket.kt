package io.horizontalsystems.xrateskit.entities

import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import java.math.BigDecimal


//-----------------------------------------------------------
@Entity(indices = [Index(value = ["currencyCode", "timePeriod"], unique = true)])
data class GlobalCoinMarketPointInfo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val currencyCode: String,
    val timestamp: Long,
    val timePeriod: TimePeriod
){
    @Ignore
    val points: MutableList<GlobalCoinMarketPoint> = mutableListOf()
}

//-----------------------------------------------------------
@Entity(foreignKeys = [
    ForeignKey(
        entity = GlobalCoinMarketPointInfo::class,
        parentColumns = ["id"],
        childColumns = ["pointInfoId"],
        onDelete = CASCADE)]
)
data class GlobalCoinMarketPoint(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long,
    val volume24h: BigDecimal,
    val marketCap: BigDecimal,
    val btcDominance: BigDecimal = BigDecimal.ZERO,
    val defiMarketCap: BigDecimal = BigDecimal.ZERO,
    val defiTvl: BigDecimal = BigDecimal.ZERO,
    var pointInfoId: Long = 0
)

//-----------------------------------------------------------
data class GlobalCoinMarket(
    val currencyCode: String,
    val volume24h: BigDecimal,
    val volume24hDiff24h: BigDecimal,
    val marketCap: BigDecimal,
    val marketCapDiff24h: BigDecimal,
    val btcDominance: BigDecimal = BigDecimal.ZERO,
    val btcDominanceDiff24h: BigDecimal = BigDecimal.ZERO,
    val defiMarketCap: BigDecimal = BigDecimal.ZERO,
    val defiMarketCapDiff24h: BigDecimal = BigDecimal.ZERO,
    val defiTvl: BigDecimal = BigDecimal.ZERO,
    val defiTvlDiff24h: BigDecimal = BigDecimal.ZERO,
    val globalCoinMarketPoints: List<GlobalCoinMarketPoint>
){

    companion object {
        fun calculateData(currencyCode: String, globalMarketPoints: List<GlobalCoinMarketPoint>): GlobalCoinMarket {

            var marketCap = BigDecimal.ZERO
            var marketCapDiff = BigDecimal.ZERO
            var defiMarketCap = BigDecimal.ZERO
            var defiMarketCapDiff = BigDecimal.ZERO
            var volume24h = BigDecimal.ZERO
            var volume24hDiff = BigDecimal.ZERO
            var btcDominance = BigDecimal.ZERO
            var btcDominanceDiff = BigDecimal.ZERO
            var tvl = BigDecimal.ZERO
            var tvlDiff = BigDecimal.ZERO

            if(globalMarketPoints.isNotEmpty()){
                val startingPoint = globalMarketPoints.first()
                val endingPoint = globalMarketPoints.last()

                marketCap = endingPoint.marketCap
                marketCapDiff = calculateDiff(startingPoint.marketCap, marketCap)

                defiMarketCap = endingPoint.defiMarketCap
                defiMarketCapDiff = calculateDiff(startingPoint.defiMarketCap, defiMarketCap)

                volume24h = endingPoint.volume24h
                volume24hDiff = calculateDiff(startingPoint.volume24h, volume24h)

                btcDominance = endingPoint.btcDominance
                btcDominanceDiff = calculateDiff(startingPoint.btcDominance, btcDominance)

                tvl = endingPoint.defiTvl
                tvlDiff = calculateDiff(startingPoint.defiTvl, tvl)
            }

            return GlobalCoinMarket(currencyCode, volume24h, volume24hDiff, marketCap, marketCapDiff, btcDominance,
                btcDominanceDiff, defiMarketCap, defiMarketCapDiff, tvl, tvlDiff, globalMarketPoints)

        }

        private fun calculateDiff(sourceValue: BigDecimal, targetValue: BigDecimal): BigDecimal {
            return if(sourceValue.compareTo(BigDecimal.ZERO) != 0 )
                    ((targetValue - sourceValue) * BigDecimal(100))/sourceValue
            else BigDecimal.ZERO
        }
    }
}
