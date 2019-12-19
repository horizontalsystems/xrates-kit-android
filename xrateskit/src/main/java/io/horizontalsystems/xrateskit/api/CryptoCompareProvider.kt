package io.horizontalsystems.xrateskit.api

import com.eclipsesource.json.JsonObject
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.IChartInfoProvider
import io.horizontalsystems.xrateskit.core.IHistoricalRateProvider
import io.horizontalsystems.xrateskit.core.IMarketInfoProvider
import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.math.BigDecimal
import java.util.*
import java.util.concurrent.TimeUnit

class CryptoCompareProvider(private val factory: Factory, private val apiManager: ApiManager, private val baseUrl: String)
    : IMarketInfoProvider, IHistoricalRateProvider, IChartInfoProvider {

    private val retryLimit = 3

    // Market Info

    override fun getMarketInfo(coins: List<String>, currency: String): Single<List<MarketInfoEntity>> {
        return Single.create<List<MarketInfoEntity>> { emitter ->
            try {
                val codes = coins.joinToString(",")

                val json = apiManager.getJson("$baseUrl/data/pricemultifull?fsyms=${codes}&tsyms=${currency}")
                val data = json["RAW"].asObject()
                val list = mutableListOf<MarketInfoEntity>()

                for (coin in coins) {
                    try {
                        val dataCoin = data.get(coin).asObject()
                        val dataFiat = dataCoin.get(currency).asObject()

                        val rate = dataFiat["PRICE"].toString().toBigDecimal()
                        val rateOpen24Hour = dataFiat["OPEN24HOUR"].toString().toBigDecimal()
                        val diff = dataFiat["CHANGEPCT24HOUR"].toString().toBigDecimal()
                        val volume = dataFiat["VOLUME24HOURTO"].asDouble()
                        val mktcap = dataFiat["MKTCAP"].asDouble()
                        val supply = dataFiat["SUPPLY"].asDouble()

                        list.add(factory.createMarketInfoEntity(coin, currency, rate, rateOpen24Hour, diff, volume, mktcap, supply))
                    } catch (e: Exception) {
                        continue
                    }
                }

                emitter.onSuccess(list)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    //  Historical Rate

    override fun getHistoricalRate(coin: String, currency: String, timestamp: Long): Single<HistoricalRate> {
        val todayInSeconds = Date().time / 1000
        val sevenDaysInSeconds = 604800

        val single = Single.create<HistoricalRate> { emitter ->
            try {
                //API has records by minutes only for the last 7 days
                val rate = if (todayInSeconds - timestamp < sevenDaysInSeconds){
                    getByMinute(coin, currency, timestamp)
                } else  {
                    getByHour(coin, currency, timestamp)
                }
                emitter.onSuccess(rate)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }

        return singleWithRetry(single)
    }

    private fun getByMinute(coin: String, currency: String, timestamp: Long): HistoricalRate {
        val response = apiManager.getJson("$baseUrl/data/v2/histominute?fsym=${coin}&tsym=${currency}&limit=1&toTs=$timestamp")
        val value = parseValue(response)

        return factory.createHistoricalRate(coin, currency, value, timestamp)
    }

    private fun getByHour(coin: String, currency: String, timestamp: Long): HistoricalRate {
        val response = apiManager.getJson("$baseUrl/data/v2/histohour?fsym=${coin}&tsym=${currency}&limit=1&toTs=$timestamp")
        val value = parseValue(response)

        return factory.createHistoricalRate(coin, currency, value, timestamp)
    }

    private fun parseValue(jsonObject: JsonObject): BigDecimal {
        val dataObject = CryptoCompareResponse.parseData(jsonObject)

        val data = dataObject["Data"].asArray()
        val data1 = data.first().asObject()
        val data2 = data.first().asObject()

        return valueAverage(
                data1["open"].asDouble() + data1["close"].asDouble(),
                data2["open"].asDouble() + data2["close"].asDouble()
        )
    }

    //  Chart Points

    override fun getChartPoints(chartPointKey: ChartInfoKey): Single<List<ChartPointEntity>> {
        val coin = chartPointKey.coin
        val currency = chartPointKey.currency
        val chartType = chartPointKey.chartType

        val single = Single.create<List<ChartPointEntity>> { emitter ->
            try {
                val response = apiManager.getJson("$baseUrl/data/v2/${chartType.resource}?fsym=$coin&tsym=$currency&aggregate=${chartType.interval}&limit=${chartType.points}")
                val dataObject = CryptoCompareResponse.parseData(response)
                val result = dataObject["Data"].asArray().map { it.asObject() }
                val stats = mutableListOf<ChartPointEntity>()

                for (data in result) {
                    val value = valueAverage(data["open"].asDouble() + data["close"].asDouble())

                    stats.add(ChartPointEntity(
                            chartType,
                            coin,
                            currency,
                            value,
                            data["time"].asLong())
                    )
                }

                emitter.onSuccess(stats)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }

        return singleWithRetry(single)
    }

    private fun <T> singleWithRetry(single: Single<T>): Single<T> {
        return single.retryWhen { errors: Flowable<Throwable> ->
            errors.zipWith(
                Flowable.range(1, retryLimit + 1),
                BiFunction<Throwable, Int, Int> { error: Throwable, retryCount: Int ->
                    if (error is CryptoCompareError.ApiRequestLimitExceeded && retryCount <= retryLimit) {
                        retryCount
                    } else {
                        throw error
                    }
                }
            ).flatMap { retryCount ->
                //exponential delay 3, 6, 9
                val delay = 3.toDouble().times(retryCount.toDouble()).toLong()
                Flowable.timer(delay, TimeUnit.SECONDS)
            }
        }
    }

    private fun valueAverage(vararg value: Double): BigDecimal {
        return (value.sum() / (value.size * 2)).toBigDecimal()
    }
}
