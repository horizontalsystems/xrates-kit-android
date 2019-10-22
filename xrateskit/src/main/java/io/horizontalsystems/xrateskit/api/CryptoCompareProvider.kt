package io.horizontalsystems.xrateskit.api

import com.eclipsesource.json.JsonObject
import io.horizontalsystems.xrateskit.core.*
import io.horizontalsystems.xrateskit.entities.ChartPointEntity
import io.horizontalsystems.xrateskit.entities.ChartPointKey
import io.horizontalsystems.xrateskit.entities.HistoricalRate
import io.horizontalsystems.xrateskit.entities.MarketStats
import io.reactivex.Single
import java.math.BigDecimal

class CryptoCompareProvider(
        private val factory: Factory,
        private val apiManager: ApiManager,
        private val baseUrl: String)
    : ILatestRateProvider, IHistoricalRateProvider, IChartPointProvider, IMarketStatsProvider {

    // Latest Rate

    override fun getLatestRates(coins: List<String>, currency: String): Single<Map<String, String>> {
        return Single.create<Map<String, String>> { emitter ->
            try {
                val coinsCodes = coins.joinToString(",")
                val json = apiManager.getJson("$baseUrl/data/pricemulti?fsyms=$coinsCodes&tsyms=${currency}")
                val list = mutableMapOf<String, String>()

                for (coin in coins) {
                    val rate = json.get(coin) ?: continue
                    val value = rate.asObject().get(currency).toString()

                    list[coin] = value
                }

                emitter.onSuccess(list)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    //  Historical Rate

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

        return factory.createHistoricalRate(coin, currency, value, timestamp)
    }

    private fun getByHour(coin: String, currency: String, timestamp: Long): HistoricalRate {
        val response = apiManager.getJson("$baseUrl/data/v2/histohour?fsym=${coin}&tsym=${currency}&limit=1&toTs=$timestamp")
        val value = parseValue(response)

        return factory.createHistoricalRate(coin, currency, value, timestamp)
    }

    private fun parseValue(jsonObject: JsonObject): BigDecimal {
        val data = jsonObject["Data"].asObject()["Data"].asArray()
        val data1 = data.first().asObject()
        val data2 = data.first().asObject()

        return valueAverage(
                data1["open"].asDouble(),
                data1["close"].asDouble(),
                data2["open"].asDouble(),
                data2["close"].asDouble()
        )
    }

    //  Chart Points

    override fun getChartPoints(chartPointKey: ChartPointKey): Single<List<ChartPointEntity>> {
        val coin = chartPointKey.coin
        val currency = chartPointKey.currency
        val chartType = chartPointKey.chartType

        return Single.create<List<ChartPointEntity>> { emitter ->
            try {
                val response = apiManager.getJson("$baseUrl/data/v2/${chartType.resource}?fsym=$coin&tsym=$currency&aggregate=${chartType.interval}&limit=${chartType.points}")
                val result = response["Data"].asObject()["Data"].asArray().map { it.asObject() }
                val stats = mutableListOf<ChartPointEntity>()

                for (data in result) {
                    val value = valueAverage(
                            data["open"].asDouble(),
                            data["close"].asDouble()
                    )

                    stats.add(ChartPointEntity(chartType, coin, currency, value, data["time"].asLong()))
                }

                emitter.onSuccess(stats)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    //  Market Stats

    override fun getMarketStats(coin: String, currency: String): Single<MarketStats> {
        return Single.create<MarketStats> { emitter ->
            try {
                val response = apiManager.getJson("$baseUrl/data/pricemultifull?fsym=$coin&tsym=$currency")
                val data = response["RAW"].asObject()[coin].asObject()[currency].asObject()

                val volume = data["VOLUMEDAYTO"].asDouble()
                val mktcap = data["MKTCAP"].asDouble()
                val supply = data["SUPPLY"].asDouble()

                emitter.onSuccess(factory.createMarketCap(coin, currency, volume, mktcap, supply))
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    private fun valueAverage(vararg value: Double): BigDecimal {
        return (value.sum() / (value.size * 2)).toBigDecimal()
    }
}
