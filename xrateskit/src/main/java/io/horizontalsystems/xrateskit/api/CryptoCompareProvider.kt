package io.horizontalsystems.xrateskit.api

import android.util.Log
import com.eclipsesource.json.JsonObject
import io.horizontalsystems.xrateskit.core.*
import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Single
import java.math.BigDecimal
import java.util.*

class CryptoCompareProvider(private val factory: Factory, private val apiManager: ApiManager, private val baseUrl: String)
    : IMarketInfoProvider, IHistoricalRateProvider, IChartInfoProvider, ICryptoNewsProvider, ITopListProvider {

    // Market Info

    override fun getMarketInfo(coins: List<String>, currency: String): Single<List<MarketInfoEntity>> {
        return Single.create { emitter ->
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

        return Single.create { emitter ->
            try {
                //API has records by minutes only for the last 7 days
                val rate = if (todayInSeconds - timestamp < sevenDaysInSeconds) {
                    getByMinute(coin, currency, timestamp)
                } else {
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
        val dataObject = CryptoCompareResponse.parseData(jsonObject)

        val data = dataObject["Data"].asArray()
        val dataLast = data.last().asObject()
        return dataLast["close"].asDouble().toBigDecimal()
    }

    //  Chart Points

    override fun getChartPoints(chartPointKey: ChartInfoKey): Single<List<ChartPointEntity>> {
        val coin = chartPointKey.coin
        val currency = chartPointKey.currency
        val chartType = chartPointKey.chartType

        return Single.create { emitter ->
            try {
                val response = apiManager.getJson("$baseUrl/data/v2/${chartType.resource}?fsym=$coin&tsym=$currency&aggregate=${chartType.interval}&limit=${chartType.points}")
                val dataObject = CryptoCompareResponse.parseData(response)
                val result = dataObject["Data"].asArray().map { it.asObject() }
                val stats = mutableListOf<ChartPointEntity>()

                for (data in result) {
                    val value = valueAverage(data["open"].asDouble() + data["close"].asDouble())
                    val volume = data["volumefrom"].asDouble().toBigDecimal()

                        stats.add(ChartPointEntity(
                            chartType,
                            coin,
                            currency,
                            value,
                            volume,
                            data["time"].asLong())
                    )
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

    //  CryptoNews

    override fun getNews(categories: String): Single<List<CryptoNews>> {
        return Single.create { emitter ->
            try {
                val json = apiManager.getJson("$baseUrl/data/v2/news/?categories=${categories}&excludeCategories=Sponsored")
                val data = json["Data"].asArray()
                val list = mutableListOf<CryptoNews>()

                for (item in data) {
                    try {
                        val news = item.asObject()

                        val id = news.get("id").asString().toInt()
                        val time = news.get("published_on").asLong()
                        val imageUrl = news.get("imageurl").asString()
                        val title = news.get("title").asString()
                        val url = news.get("url").asString()
                        val body = news.get("body").asString()
                        val types = news.get("categories").asString().split("|")

                        list.add(factory.createCryptoNews(id, time, imageUrl, title, url, body, types))
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

    override fun getTopListCoins(currency: String): Single<List<CoinInfo>> {
        return Single.create { emitter ->
            try {
                val json = apiManager.getJson("$baseUrl/data/top/mktcapfull?limit=100&tsym=${currency}")
                val data = json["Data"].asArray()
                val list = mutableListOf<CoinInfo>()

                for (item in data) {
                    try {
                        val coinCode = item.asObject().get("CoinInfo").asObject().get("Name").asString()
                        val coinName = item.asObject().get("CoinInfo").asObject().get("FullName").asString()
                        list.add(CoinInfo(coinCode, coinName))
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

    override fun getPriceInfo(coins: List<CoinInfo>, currency: String): Single<List<PriceInfo>> {
        return Single.create { emitter ->
            try {
                val codes = coins.joinToString(",") { it.coinCode }

                val json = apiManager.getJson("$baseUrl/data/pricemultifull?fsyms=${codes}&tsyms=${currency}")
                val data = json["RAW"].asObject()
                val list = mutableListOf<PriceInfo>()

                for (coin in coins) {
                    try {
                        val dataCoin = data.get(coin.coinCode).asObject()
                        val dataFiat = dataCoin.get(currency).asObject()

                        val rate = dataFiat["PRICE"].toString().toBigDecimal()
                        val diff = dataFiat["CHANGEPCT24HOUR"].toString().toBigDecimal()

                        list.add(PriceInfo(coin.coinCode, coin.coinName, rate, diff))
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

}
