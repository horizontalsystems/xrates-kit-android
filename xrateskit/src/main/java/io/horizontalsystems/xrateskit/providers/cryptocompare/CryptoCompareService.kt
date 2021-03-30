package io.horizontalsystems.xrateskit.providers.cryptocompare

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface CryptoCompareService {

    @GET("data/price")
    fun price(
        @Query("api_key") apiKey: String,
        @Query("fsym") sourceCurrency: String,
        @Query("tsyms") targetCurrency: String
    ): Single<Map<String, Double>>

    @GET("data/v2/news/")
    fun news(
        @Query("api_key") apiKey: String,
        @Query("categories") categories: String,
        @Query("excludeCategories") excludeCategories: String,
    ): Single<NewsResponse>

}

data class NewsResponse(
    val Data: List<NewsItem>
)

data class NewsItem(
    val id: Int,
    val published_on: Long,
    val imageurl: String,
    val title: String,
    val url: String,
    val body: String,
    val categories: String,
)
