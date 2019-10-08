package io.horizontalsystems.xrateskit.api

import com.eclipsesource.json.JsonObject
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.IHistoricalRateProvider
import io.horizontalsystems.xrateskit.core.ILatestRateProvider
import io.horizontalsystems.xrateskit.storage.HistoricalRate
import io.horizontalsystems.xrateskit.storage.LatestRate
import io.reactivex.Observable
import io.reactivex.Single

class CryptoCompareProvider(
        private val factory: Factory,
        private val apiManager: ApiManager,
        private val baseUrl: String)
    : ILatestRateProvider, IHistoricalRateProvider {

    override fun getLatestRate(coins: List<String>, currency: String): Observable<LatestRate> {
        return Observable.create<LatestRate> { subscriber ->
            try {
                val fSyms = coins.joinToString(",")
                val jsonObject = apiManager.getJson("$baseUrl/data/pricemulti?fsyms=$fSyms&tsyms=${currency}")

                for (coin in coins) {
                    val rateObject = jsonObject.get(coin).asObject()
                    val value = rateObject.get(currency).toString()

                    subscriber.onNext(factory.createLatestRate(coin, currency, value.toBigDecimal()))
                }

                subscriber.onComplete()
            } catch (e: Exception) {
                subscriber.onError(e)
            }
        }
    }

    override fun getHistoricalRate(coin: String, currency: String, timestamp: Long): Single<HistoricalRate> {
        return Single.create { emitter ->
            try {
                val rate = try {
                    getByMinute(coin, currency, timestamp)
                } catch (e: Exception) {
                    getByHour(coin, currency, timestamp)
                }

                emitter.onSuccess(rate)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    private fun getByMinute(coin: String, currency: String, timestamp: Long): HistoricalRate {
        val response = apiManager.getJson("$baseUrl/data/v2/histominute?fsym=${coin}&tsym=${currency}&limit=1&toTs=$timestamp")
        val value = parseValue(response)

        return factory.createHistoricalRate(coin, currency, value.toBigDecimal(), timestamp)
    }

    private fun getByHour(coin: String, currency: String, timestamp: Long): HistoricalRate {
        val response = apiManager.getJson("$baseUrl/data/v2/histohour?fsym=${coin}&tsym=${currency}&limit=1&toTs=$timestamp")
        val value = parseValue(response)

        return factory.createHistoricalRate(coin, currency, value.toBigDecimal(), timestamp)
    }

    private fun parseValue(jsonObject: JsonObject): Double {
        val data = jsonObject["Data"].asObject()["Data"].asArray()
        val data1 = data.first().asObject()
        val data2 = data.first().asObject()

        val data1Open = data1["open"].asDouble()
        val data1Close = data1["open"].asDouble()

        val data2Open = data2["open"].asDouble()
        val data2Close = data2["open"].asDouble()

        return (data1Open + data1Close + data2Open + data2Close) / (data.size() * 2)
    }
}
