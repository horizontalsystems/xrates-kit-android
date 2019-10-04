package io.horizontalsystems.xrateskit.core

import io.horizontalsystems.xrateskit.storage.Rate
import io.reactivex.Observable

interface IStorage {
    fun saveRate(rate: Rate)
    fun getRate(coin: String, currency: String): Rate?
}

interface ILatestRateProvider {
    fun getLatestRate(coins: List<String>, currency: String): Observable<Rate>
}

interface ISyncCompletionListener {
    fun onSuccess()
    fun onFail()
}
