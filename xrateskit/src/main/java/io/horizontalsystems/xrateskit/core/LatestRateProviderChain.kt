package io.horizontalsystems.xrateskit.core

import io.horizontalsystems.xrateskit.storage.Rate
import io.reactivex.Observable

class LatestRateProviderChain : ILatestRateProvider {
    private val providers = mutableListOf<ILatestRateProvider>()

    fun add(provider: ILatestRateProvider) {
        providers.add(provider)
    }

    override fun getLatestRate(coins: List<String>, currency: String): Observable<Rate> {
        return getLatestRate(providers, coins, currency)
    }

    //  Private

    private fun getLatestRate(providers: List<ILatestRateProvider>, coins: List<String>, currency: String): Observable<Rate> {
        val currentProvider = providers.firstOrNull()
                ?: return Observable.error(Exception("Invalid provider"))

        return currentProvider
                .getLatestRate(coins, currency)
                .doOnError {
                    getLatestRate(providers.drop(0), coins, currency)
                }
    }
}
