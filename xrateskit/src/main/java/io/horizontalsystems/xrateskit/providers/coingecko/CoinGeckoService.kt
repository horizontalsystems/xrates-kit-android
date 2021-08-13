package io.horizontalsystems.xrateskit.providers.coingecko

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.math.BigDecimal

interface CoinGeckoService {

    @GET("coins/{coinId}/market_chart")
    fun coinMarketChart(
        @Path("coinId") coinId: String,
        @Query("vs_currency") vs_currency: String,
        @Query("days") days: Int,
        @Query("interval") interval: String?,
    ): Single<Response.HistoricalMarketData>

    @GET("coins/{coinId}/market_chart/range")
    fun coinMarketChartRange(
        @Path("coinId") coinId: String,
        @Query("vs_currency") vs_currency: String,
        @Query("from") from: Long,
        @Query("to") to: Long,
    ): Single<Response.HistoricalMarketData>

    @GET("simple/price")
    fun simplePrice(
        @Query("ids") ids: String,
        @Query("vs_currencies") vs_currencies: String,
        @Query("include_market_cap") include_market_cap: String,
        @Query("include_24hr_vol") include_24hr_vol: String,
        @Query("include_24hr_change") include_24hr_change: String,
        @Query("include_last_updated_at") include_last_updated_at: String,
    ): Single<Map<String, Map<String, BigDecimal>>>

    @GET("coins/{coinId}")
    fun coin(
        @Path("coinId") coinId: String,
        @Query("tickers") tickers: String,
        @Query("localization") localization: String,
        @Query("sparkline") sparkline: String,
    ): Single<Response.Coin>

    @GET("coins/markets")
    fun coinsMarkets(
        @Query("vs_currency") vs_currency: String,
        @Query("ids") ids: String?,
        @Query("category") category: String?,
        @Query("order") order: String?,
        @Query("per_page") per_page: Int?,
        @Query("page") page: Int?,
        @Query("price_change_percentage") price_change_percentage: String?,
    ): Single<List<Response.CoinMarket>>

    object Response {
        data class CoinMarket(
            val id: String,
            val symbol: String,
            val name: String,
            val current_price: BigDecimal?,
            val market_cap: BigDecimal?,
            val fully_diluted_valuation: BigDecimal?,
            val total_supply: BigDecimal?,
            val circulating_supply: BigDecimal?,
            val total_volume: BigDecimal?,
            val ath_change_percentage: BigDecimal,
            val atl_change_percentage: BigDecimal,
            val price_change_24h: BigDecimal?,
            val price_change_percentage_24h: BigDecimal?,
            val price_change_percentage_1h_in_currency: BigDecimal?,
            val price_change_percentage_7d_in_currency: BigDecimal?,
            val price_change_percentage_14d_in_currency: BigDecimal?,
            val price_change_percentage_30d_in_currency: BigDecimal?,
            val price_change_percentage_200d_in_currency: BigDecimal?,
            val price_change_percentage_1y_in_currency: BigDecimal?,
        )

        data class HistoricalMarketData(
            val prices: List<List<BigDecimal>>,
            val market_caps: List<List<BigDecimal>>,
            val total_volumes: List<List<BigDecimal>>,
        )

        data class Coin(
            val id: String,
            val symbol: String,
            val name: String,
            val genesis_date: String?,
            val description: Map<String, String>,
            val links: Links,
            val platforms: Map<String, String>,
            val market_data: MarketData,
            val tickers: List<Ticker>
        ) {
            data class MarketData(
                val current_price: Map<String, BigDecimal>,
                val high_24h: Map<String, BigDecimal>,
                val low_24h: Map<String, BigDecimal>,
                val market_cap: Map<String, BigDecimal>,
                val market_cap_rank: Int?,
                val fully_diluted_valuation: Map<String, BigDecimal>,
                val market_cap_change_percentage_24h: BigDecimal?,
                val total_volume: Map<String, BigDecimal>,
                val circulating_supply: BigDecimal?,
                val total_supply: BigDecimal?,
                val price_change_percentage_1h_in_currency: Map<String, BigDecimal>,
                val price_change_percentage_24h_in_currency: Map<String, BigDecimal>,
                val price_change_percentage_7d_in_currency: Map<String, BigDecimal>,
                val price_change_percentage_14d_in_currency: Map<String, BigDecimal>,
                val price_change_percentage_30d_in_currency: Map<String, BigDecimal>,
                val price_change_percentage_200d_in_currency: Map<String, BigDecimal>,
                val price_change_percentage_1y_in_currency: Map<String, BigDecimal>,
            )

            data class Links(
                val homepage: List<String>,
                val twitter_screen_name: String?,
                val telegram_channel_identifier: String?,
                val subreddit_url: String?,
                val repos_url: Map<String, List<String>>,
            )

            data class Ticker(
                val base: String,
                val target: String,
                val market: Market,
                val last: BigDecimal,
                val volume: BigDecimal,
            ) {
                data class Market(val name: String, val identifier: String)
            }
        }
    }
}
