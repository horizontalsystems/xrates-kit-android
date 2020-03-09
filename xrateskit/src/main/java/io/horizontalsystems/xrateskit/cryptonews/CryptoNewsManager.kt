package io.horizontalsystems.xrateskit.cryptonews

import io.horizontalsystems.xrateskit.api.CryptoCompareProvider
import io.horizontalsystems.xrateskit.entities.CryptoNews
import io.reactivex.Single
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class CryptoNewsManager(private val expirationMinutes: Long, private val newsProvider: CryptoCompareProvider) {

    private val categories = listOf("Regulation")
    private val news = ConcurrentHashMap<String, List<CryptoNews>>()
    private val newsLastUpdate = ConcurrentHashMap<String, Long>()

    fun getNews(coin: String, timestamp: Long): Single<List<CryptoNews>> {
        return nonExpiredNews(coin) ?: fetchFreshNews(coin, timestamp)
    }

    private fun nonExpiredNews(coin: String): Single<List<CryptoNews>>? {
        val newsCache = news[coin]
        if (newsCache == null || newsCache.isEmpty()) {
            return null
        }

        newsLastUpdate[coin]?.let { updatedTime ->
            val now = Date().time / 1000
            if (now > updatedTime + expirationMinutes * 60) {
                return null
            }
        }

        return Single.just(newsCache)
    }

    private fun fetchFreshNews(coin: String, timestamp: Long): Single<List<CryptoNews>> {
        return newsProvider
            .getNews(categories.joinToString(","), timestamp)
            .doOnSuccess {
                news[coin] = it
                newsLastUpdate[coin] = Date().time / 1000
            }
    }
}