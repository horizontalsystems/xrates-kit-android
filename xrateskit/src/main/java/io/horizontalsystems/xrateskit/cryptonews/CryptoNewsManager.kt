package io.horizontalsystems.xrateskit.cryptonews

import io.horizontalsystems.xrateskit.providers.cryptocompare.CryptoCompareProvider
import io.horizontalsystems.xrateskit.entities.CryptoNews
import io.reactivex.Single
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class CryptoNewsManager(private val expirationMinutes: Long, private val newsProvider: CryptoCompareProvider) {

    private val altcoinCategories = listOf("Altcoin", "Trading")
    private val registeredCoinList = listOf(
        "BTC",
        "BCH",
        "ETH",
        "DASH",
        "USDT"
    )

    private val categories = listOf("Regulation")
    private val news = ConcurrentHashMap<String, List<CryptoNews>>()
    private val newsLastUpdate = ConcurrentHashMap<String, Long>()

    fun getNews(coin: String): Single<List<CryptoNews>> {
        return nonExpiredNews(coin) ?: fetchFreshNews(coin)
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

    private fun fetchFreshNews(coin: String): Single<List<CryptoNews>> {
        val newsFilter = if (registeredCoinList.contains(coin)) listOf(coin) else altcoinCategories
        val categoriesOfNews = newsFilter + categories

        return newsProvider
            .getNews(categoriesOfNews.joinToString(","))
            .doOnSuccess {
                news[coin] = it
                newsLastUpdate[coin] = Date().time / 1000
            }
    }
}