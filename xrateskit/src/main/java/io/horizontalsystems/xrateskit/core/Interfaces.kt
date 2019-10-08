package io.horizontalsystems.xrateskit.core

import io.horizontalsystems.xrateskit.storage.HistoricalRate
import io.horizontalsystems.xrateskit.storage.LatestRate
import io.reactivex.Observable
import io.reactivex.Single

interface IStorage {
    fun saveLatestRate(rate: LatestRate)
    fun getLatestRate(coin: String, currency: String): LatestRate?
    fun saveHistoricalRate(rate: HistoricalRate)
    fun getHistoricalRate(coin: String, currency: String, timestamp: Long): HistoricalRate?
}

interface ILatestRateProvider {
    fun getLatestRate(coins: List<String>, currency: String): Observable<LatestRate>
}

interface IHistoricalRateProvider {
    fun getHistoricalRate(coin: String, currency: String, timestamp: Long): Single<HistoricalRate>
}

interface ISyncCompletionListener {
    fun onSuccess()
    fun onFail()
}
