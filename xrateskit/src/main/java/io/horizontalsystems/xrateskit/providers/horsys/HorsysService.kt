package io.horizontalsystems.xrateskit.providers.horsys

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.math.BigDecimal

interface HorsysService {
    @GET("markets/global/{timePeriod}")
    fun globalCoinMarketPoints(@Path("timePeriod") timePeriod: String, @Query("currency_code") currencyCode: String): Single<List<Response.GlobalCoinMarketPoint>>
}

object Response {
    data class GlobalCoinMarketPoint(
        val currency_code: String,
        val timestamp: Long,
        var volume24h: BigDecimal,
        var market_cap: BigDecimal,
        var dominance_btc: BigDecimal = BigDecimal.ZERO,
        var market_cap_defi: BigDecimal = BigDecimal.ZERO,
        var tvl: BigDecimal = BigDecimal.ZERO,
    )
}