package io.horizontalsystems.xrateskit.api

import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.ILatestRateProvider
import io.horizontalsystems.xrateskit.storage.Rate
import io.reactivex.Observable

class CryptoCompareProvider(
        private val factory: Factory,
        private val apiManager: ApiManager,
        private val baseUrl: String)
    : ILatestRateProvider {

    override fun getLatestRate(coins: List<String>, currency: String): Observable<Rate> {
        return Observable.create<Rate> { subscriber ->
            try {
                val fSyms = coins.joinToString(",")
                val jsonObject =
                        apiManager.getJson("$baseUrl/data/pricemulti?fsyms=$fSyms&tsyms=${currency}")

                for (coin in coins) {
                    val rateObject = jsonObject.get(coin).asObject()
                    val value = rateObject.get(currency).asDouble()

                    val data = CryptoCompareResponse(coin, currency, value)
                    subscriber.onNext(factory.createRate(data))
                }

                subscriber.onComplete()
            } catch (e: Exception) {
                subscriber.onError(e)
            }
        }
    }
}
