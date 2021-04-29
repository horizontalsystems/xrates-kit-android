package io.horizontalsystems.xrateskit.cryptonews

import io.horizontalsystems.xrateskit.providers.cryptocompare.CryptoCompareProvider
import io.horizontalsystems.xrateskit.entities.CryptoNews
import io.reactivex.Single
import java.sql.Timestamp
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class CryptoNewsManager(private val newsProvider: CryptoCompareProvider) {

    fun getNewsAsync(latestTimestamp: Long?): Single<List<CryptoNews>> {
        return newsProvider.getNewsAsync(latestTimestamp)
    }
}