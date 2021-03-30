package io.horizontalsystems.xrateskit.providers.coingecko

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.math.BigDecimal

interface CoinGeckoService {
    @GET("coins/{coinId}/market_chart/range")
    fun historicalMarketData(
        @Path("coinId") coinId: String,
        @Query("vs_currency") vs_currency: String,
        @Query("from") from: Long,
        @Query("to") to: Long,
    ): Single<HistoricalMarketData>

    @GET("simple/price")
    fun simplePrice(
        @Query("ids") ids: String,
        @Query("vs_currencies") vs_currencies: String,
        @Query("include_market_cap") include_market_cap: String,
        @Query("include_24hr_vol") include_24hr_vol: String,
        @Query("include_24hr_change") include_24hr_change: String,
        @Query("include_last_updated_at") include_last_updated_at: String,
    ): Single<Map<String, Map<String, BigDecimal>>>
}

data class HistoricalMarketData(
    val prices: List<List<BigDecimal>>
)
