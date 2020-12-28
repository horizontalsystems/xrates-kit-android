package io.horizontalsystems.xrateskit.api

import com.eclipsesource.json.JsonObject
import io.horizontalsystems.xrateskit.core.*
import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Single
import java.math.BigDecimal
import java.util.*
import java.util.logging.Logger

class CryptoCompareProvider(
        private val factory: Factory,
        private val apiManager: ApiManager,
        private val baseUrl: String,
        private val apiKey: String,
        private val indicatorPointCount: Int)
    : IHistoricalRateProvider, IChartInfoProvider, ICryptoNewsProvider, ITopMarketsProvider, IFiatXRatesProvider {

    private val logger = Logger.getLogger("CryptoCompareProvider")

    // Market Info

    fun getMarketInfo(coins: List<Coin>, currency: String): Single<List<MarketInfoEntity>> {

        if(coins.isEmpty())
            return Single.just(Collections.emptyList())

        return Single.create { emitter ->
            try {
                val coinCodeList = coins.map { coin -> coin.code }
                val codes = coinCodeList.joinToString(",")

                val json = apiManager.getJson("$baseUrl/data/pricemultifull?api_key=${apiKey}&fsyms=${codes}&tsyms=${currency}")
                val data = json["RAW"].asObject()
                val list = mutableListOf<MarketInfoEntity>()

                for (coin in coinCodeList) {
                    try {
                        val dataCoin = data.get(coin).asObject()
                        val dataFiat = dataCoin.get(currency).asObject()

                        val rate = dataFiat["PRICE"].toString().toBigDecimal()
                        val rateOpenDay = dataFiat["OPENDAY"].toString().toBigDecimal()
                        val diff = dataFiat["CHANGEPCTDAY"].toString().toBigDecimal()
                        val volume = dataFiat["VOLUME24HOURTO"].asDouble().toBigDecimal()
                        val mktcap = dataFiat["MKTCAP"].asDouble().toBigDecimal()
                        val supply = dataFiat["SUPPLY"].asDouble().toBigDecimal()

                        list.add(factory.createMarketInfoEntity(coin, currency, rate, rateOpenDay, diff, volume, mktcap, supply))
                    } catch (e: Exception) {
                        continue
                    }
                }

                emitter.onSuccess(list)

            } catch (e: Exception) {
                logger.severe(e.message)
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
        val response = apiManager.getJson("$baseUrl/data/v2/histominute?api_key=${apiKey}&fsym=${coin}&tsym=${currency}&limit=1&toTs=$timestamp")
        val value = parseValue(response)

        return factory.createHistoricalRate(coin, currency, value, timestamp)
    }

    private fun getByHour(coin: String, currency: String, timestamp: Long): HistoricalRate {
        val response = apiManager.getJson("$baseUrl/data/v2/histohour?api_key=${apiKey}&fsym=${coin}&tsym=${currency}&limit=1&toTs=$timestamp")
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
        return Single.create { emitter ->
            try {
                val stats = fetchChartPoints(mutableListOf(), chartPointKey, chartPointKey.chartType.points + indicatorPointCount, toTimestamp = null)
                val sorted = stats.sortedBy { it.timestamp }
                emitter.onSuccess(sorted)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    private fun fetchChartPoints(stats: MutableList<ChartPointEntity>, chartPointKey: ChartInfoKey, limit: Int, toTimestamp: Long?): MutableList<ChartPointEntity> {
        val coin = chartPointKey.coin
        val currency = chartPointKey.currency
        val chartType = chartPointKey.chartType

        var baseUrl = "$baseUrl/data/v2/${chartType.resource}?api_key=${apiKey}&fsym=$coin&tsym=$currency&aggregate=${chartType.interval}"
        if (toTimestamp != null) {
            baseUrl += "&toTs=${toTimestamp}"
        }

        val response = apiManager.getJson("$baseUrl&limit=${limit}")
        val dataObject = CryptoCompareResponse.parseData(response)
        val result = dataObject["Data"].asArray().map { it.asObject() }

        for (data in result) {
            val value = data["open"].asDouble().toBigDecimal()
            val volume = data["volumeto"].asDouble().toBigDecimal()
            val timestamp = data["time"].asLong()

            stats.add(ChartPointEntity(
                chartType,
                coin,
                currency,
                value,
                volume,
                timestamp)
            )
        }

        if (stats.size < limit) {
            val newLimit = limit - stats.size
            val timeFrom = dataObject["TimeFrom"].asInt()
            val newTimestamp = timeFrom - chartType.expirationInterval * newLimit

            return fetchChartPoints(stats, chartPointKey, newLimit, newTimestamp)
        }

        return stats
    }

    //  CryptoNews

    override fun getNews(categories: String): Single<List<CryptoNews>> {
        return Single.create { emitter ->
            try {
                val json = apiManager.getJson("$baseUrl/data/v2/news/?api_key=${apiKey}&categories=${categories}&excludeCategories=Sponsored")
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

    override fun getTopMarketsAsync(itemsCount: Int, currency: String, rateDiffPeriod: TimePeriod): Single<List<TopMarket>> {
        return Single.create { emitter ->
            try {
                val json = apiManager.getJson("$baseUrl/data/top/mktcapfull?api_key=${apiKey}&limit=$itemsCount&tsym=${currency}")
                val data = json["Data"].asArray()
                val list = mutableListOf<TopMarket>()

                for (coinData in data) {
                    try {
                        val coinInfo = coinData.asObject().get("CoinInfo").asObject()
                        val coinCode = coinInfo.get("Name").asString()
                        val coinName = coinInfo.get("FullName").asString()

                        val raw = coinData.asObject().get("RAW").asObject()
                        val fiatData = raw.get(currency).asObject()
                        val rate = fiatData["PRICE"].toString().toBigDecimal()
                        val rateOpenDay = fiatData["OPENDAY"].toString().toBigDecimal()
                        val diff = fiatData["CHANGEPCT24HOUR"].toString().toBigDecimal()
                        val volume = fiatData["VOLUME24HOURTO"].asDouble().toBigDecimal()
                        val marketCap = fiatData["MKTCAP"].asDouble().toBigDecimal()
                        val supply = fiatData["SUPPLY"].asDouble().toBigDecimal()

                        list.add(factory.createTopMarket(Coin(coinCode, coinName), currency, rate, rateOpenDay, diff, volume, marketCap, supply))
                    } catch (ex: Exception) {
                        logger.warning(ex.message)
                        continue
                    }
                }
                emitter.onSuccess(list)

            } catch (ex: Exception) {
                logger.severe(ex.message)
                emitter.onError(ex)
            }
        }
    }

    override fun getLatestFiatXRates(sourceCurrency: String, targetCurrency: String): Double {
        val response = apiManager.getJson("$baseUrl/data/price?api_key=${apiKey}&fsym=${sourceCurrency}" +
                                                  "&tsyms=${targetCurrency}")

        return response.asObject()[targetCurrency].asDouble()
    }
}
