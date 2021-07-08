package io.horizontalsystems.xrateskit.providers.horsys

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.math.BigDecimal

interface HorsysService {
    @GET("tokens/holders/{tokenAddress}")
    fun tokenHolders(@Path("tokenAddress") tokenAddress: String, @Query("limit") limit: Int): Single<List<Response.TokenHolder>>

    @GET("markets/global/{timePeriod}")
    fun globalCoinMarketPoints(@Path("timePeriod") timePeriod: String, @Query("currency_code") currencyCode: String): Single<List<Response.GlobalCoinMarketPoint>>

    @GET("markets/defi")
    fun defiTvl(
        @Query("currency_code") currencyCode: String,
        @Query("diff_period") periods: String,
        @Query("chain_filter") chainFilter: String?,
    ): Single<List<Response.DefiTvl>>

    @GET("markets/defi/{coinGeckoId}/latest")
    fun coinDefiTvl(@Path("coinGeckoId") timePeriod: String, @Query("currency_code") currencyCode: String): Single<Response.DefiTvl>

    @GET("markets/defi/{coinGeckoId}/{timePeriod}")
    fun defiTvlPoints(
        @Path("coinGeckoId") coinGeckoId: String,
        @Path("timePeriod") timePeriod: String,
        @Query("currency_code") currencyCode: String
    ): Single<List<Response.DefiTvlPoint>>
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
        val code: String?,
        var tvl: BigDecimal,
        var tvl_rank: Int?,
        var tvl_diff_1h: BigDecimal?,
        var tvl_diff_24h: BigDecimal?,
        var tvl_diff_7d: BigDecimal?,
        var tvl_diff_14d: BigDecimal?,
        var tvl_diff_30d: BigDecimal?,
        var chains: List<String>?
    )

    data class DefiTvlPoint(
        val timestamp: Long,
        val currency_code: String,
        var tvl: BigDecimal
    )

    data class TokenHolder(
        val address: String,
        var share: BigDecimal
    )
}