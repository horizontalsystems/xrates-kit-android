package io.horizontalsystems.xrateskit.providers.cryptocompare

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface CryptoCompareService {

    @GET("data/v2/news/")
    fun news(
        @Query("api_key") apiKey: String,
        @Query("feeds") feeds: String,
        @Query("extraParams") extraParams: String,
        @Query("lTs") latestTimestamp: Long?
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
    val source: String,
    val categories: String,
)
