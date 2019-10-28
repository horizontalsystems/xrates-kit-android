package io.horizontalsystems.xrateskit.api

import com.eclipsesource.json.JsonObject
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.IChartInfoProvider
import io.horizontalsystems.xrateskit.core.IHistoricalRateProvider
import io.horizontalsystems.xrateskit.core.IMarketInfoProvider
import io.horizontalsystems.xrateskit.entities.ChartPointEntity
import io.horizontalsystems.xrateskit.entities.ChartInfoKey
import io.horizontalsystems.xrateskit.entities.HistoricalRate
import io.horizontalsystems.xrateskit.entities.MarketInfoEntity
import io.reactivex.Single
import java.math.BigDecimal

class CryptoCompareProvider(private val factory: Factory, private val apiManager: ApiManager, private val baseUrl: String)
    : IMarketInfoProvider, IHistoricalRateProvider, IChartInfoProvider {

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
                        val diff = dataFiat["CHANGEPCT24HOUR"].toString().toBigDecimal()
                        val time = dataFiat["LASTUPDATE"].asLong()
                        val volume = dataFiat["VOLUME24HOURTO"].asDouble()
                        val mktcap = dataFiat["MKTCAP"].asDouble()
                        val supply = dataFiat["SUPPLY"].asDouble()

                        list.add(factory.createMarketInfoEntity(coin, currency, rate, diff, volume, mktcap, supply, time))
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

    override fun getChartPoints(chartPointKey: ChartInfoKey): Single<List<ChartPointEntity>> {
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

    private fun valueAverage(vararg value: Double): BigDecimal {
        return (value.sum() / (value.size * 2)).toBigDecimal()
    }
}
