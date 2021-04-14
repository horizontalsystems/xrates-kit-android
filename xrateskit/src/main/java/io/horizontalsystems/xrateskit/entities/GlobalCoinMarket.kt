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
                val startingPoint = globalMarketPoints.last()
                val endingPoint = globalMarketPoints.first()

                marketCap = endingPoint.marketCap
                if(startingPoint.marketCap.compareTo(BigDecimal.ZERO) != 0 )
                    marketCapDiff = ((marketCap - startingPoint.marketCap) * BigDecimal(100))/startingPoint.marketCap

                defiMarketCap = endingPoint.defiMarketCap
                if(startingPoint.defiMarketCap.compareTo(BigDecimal.ZERO) != 0 )
                    defiMarketCapDiff = ((defiMarketCap - startingPoint.defiMarketCap) * BigDecimal(100))/startingPoint.defiMarketCap

                volume24h = endingPoint.volume24h
                if(startingPoint.volume24h.compareTo(BigDecimal.ZERO) != 0 )
                    volume24hDiff = ((volume24h - startingPoint.volume24h) * BigDecimal(100))/startingPoint.volume24h

                btcDominance = endingPoint.btcDominance
                if(startingPoint.btcDominance.compareTo(BigDecimal.ZERO) != 0 )
                    btcDominanceDiff = ((btcDominance - startingPoint.btcDominance) * BigDecimal(100))/startingPoint.btcDominance

                tvl = endingPoint.defiTvl
                if(startingPoint.defiTvl.compareTo(BigDecimal.ZERO) != 0 )
                    tvlDiff = ((tvl - startingPoint.defiTvl) * BigDecimal(100))/startingPoint.defiTvl

            }

            return GlobalCoinMarket(currencyCode, volume24h, volume24hDiff, marketCap, marketCapDiff, btcDominance,
                btcDominanceDiff, defiMarketCap, defiMarketCapDiff, tvl, tvlDiff, globalMarketPoints)

        }
    }
}
