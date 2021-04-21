package io.horizontalsystems.xrateskit.providers.horsys

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.math.BigDecimal

interface HorsysService {
    @GET("markets/global/{timePeriod}")
    fun globalCoinMarketPoints(@Path("timePeriod") timePeriod: String, @Query("currency_code") currencyCode: String): Single<List<Response.GlobalCoinMarketPoint>>

    @GET("markets/defi")
    fun defiTvl(@Query("currency_code") currencyCode: String, @Query("diff_period") periods: String): Single<List<Response.DefiTvl>>
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

    data class DefiTvl(
        val currency_code: String,
        val coingecko_id: String?,
        val name: String,
        val code: String,
        var tvl: BigDecimal,
        var tvl_diff_1h: BigDecimal?,
        var tvl_diff_24h: BigDecimal?,
        var tvl_diff_7d: BigDecimal?,
        var tvl_diff_14d: BigDecimal?,
        var tvl_diff_30d: BigDecimal?,
    )
}