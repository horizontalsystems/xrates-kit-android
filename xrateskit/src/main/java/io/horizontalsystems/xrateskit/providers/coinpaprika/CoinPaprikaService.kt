package io.horizontalsystems.xrateskit.providers.coinpaprika

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.math.BigDecimal

interface CoinPaprikaService {

    @GET("global")
    fun global(): Single<Global>

    @GET("coins/{coinId}/ohlcv/historical")
    fun historicalOhlc(@Path("coinId") coinId: String, @Query("start") start: Long) : Single<List<Ohlc>>
}

data class Global(
    val volume_24h_usd: BigDecimal,
    val volume_24h_change_24h: BigDecimal,
    val market_cap_usd: BigDecimal,
    val market_cap_change_24h: BigDecimal,
    val bitcoin_dominance_percentage: BigDecimal,
)

data class Ohlc(val market_cap: Double)
