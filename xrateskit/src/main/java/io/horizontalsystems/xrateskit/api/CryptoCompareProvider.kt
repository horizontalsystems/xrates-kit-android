package io.horizontalsystems.xrateskit.api

import com.eclipsesource.json.JsonObject
import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.coins.ProviderCoinError
import io.horizontalsystems.xrateskit.coins.ProviderCoinsManager
import io.horizontalsystems.xrateskit.core.*
import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Single
import java.math.BigDecimal
import java.util.*
import java.util.logging.Logger

class CryptoCompareProvider(
        private val factory: Factory,
        private val apiManager: ApiManager,
        private val apiKey: String,
        private val indicatorPointCount: Int,
        private val providerCoinsManager: ProviderCoinsManager)
    : IInfoProvider, IHistoricalRateProvider, IChartInfoProvider, ICryptoNewsProvider, IMarketInfoProvider, IFiatXRatesProvider {

    private val logger = Logger.getLogger("CryptoCompareProvider")
    override val provider: InfoProvider = InfoProvider.CryptoCompare()

    override fun initProvider() {}
    override fun destroy() {}

    private fun getProviderCoinId(coinType: CoinType): String {
        providerCoinsManager.getProviderIds(listOf(coinType), this.provider).let {
            if(it.isNotEmpty()){
                it[0]?.let {
                    return it
                }
            }
        }
        throw ProviderCoinError.NoMatchingExternalId()
    }

    private fun getCoinType(providerCoinId: String?): CoinType {

        providerCoinId?.let {
            providerCoinsManager.getCoinTypes(it.toLowerCase(), this.provider).let { coinTypes ->
                if (coinTypes.isNotEmpty()) {
                    return coinTypes[0]
                }
            }
        }

        throw ProviderCoinError.NoMatchingExternalId()
    }
    override fun getMarketInfo(coinTypes: List<CoinType>, currency: String): Single<List<MarketInfoEntity>> {

        val coinCodeList = providerCoinsManager.getProviderIds(coinTypes, this.provider)
        if(coinCodeList.isEmpty())
            return Single.just(Collections.emptyList())

        return Single.create { emitter ->
            try {
                val codes = coinCodeList.joinToString(",")
                val json = apiManager.getJson("${provider.baseUrl}/data/pricemultifull?api_key=${apiKey}&fsyms=${codes}&tsyms=${currency}")
                val data = json["RAW"].asObject()
                val list = mutableListOf<MarketInfoEntity>()

                for (coin in coinCodeList) {
                    try {
                        val coinType = getCoinType(coin)
                        val dataCoin = data.get(coin).asObject()
                        val dataFiat = dataCoin.get(currency).asObject()

                        val rate = dataFiat["PRICE"].toString().toBigDecimal()
                        val rateOpenDay = dataFiat["OPENDAY"].toString().toBigDecimal()
                        val diff = dataFiat["CHANGEPCTDAY"].toString().toBigDecimal()
                        val volume = dataFiat["VOLUME24HOURTO"].asDouble().toBigDecimal()
                        val mktcap = dataFiat["MKTCAP"].asDouble().toBigDecimal()
                        val supply = dataFiat["SUPPLY"].asDouble().toBigDecimal()

                        list.add(factory.createMarketInfoEntity(coinType, currency, rate, rateOpenDay, diff, volume, mktcap, supply))
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

    override fun getHistoricalRate(coinType: CoinType, currency: String, timestamp: Long): Single<HistoricalRate> {
        val todayInSeconds = Date().time / 1000
        val sevenDaysInSeconds = 604800

        return Single.create { emitter ->
            try {
                //API has records by minutes only for the last 7 days
                val rate = if (todayInSeconds - timestamp < sevenDaysInSeconds) {
                    getByMinute(coinType, currency, timestamp)
                } else {
                    getByHour(coinType, currency, timestamp)
                }
                emitter.onSuccess(rate)
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    private fun getByMinute(coinType: CoinType, currency: String, timestamp: Long): HistoricalRate {
        val providerCoinId = getProviderCoinId(coinType)
        val response = apiManager.getJson("${provider.baseUrl}/data/v2/histominute?api_key=${apiKey}&fsym=${providerCoinId}&tsym=${currency}&limit=1&toTs=$timestamp")
        val value = parseValue(response)

        return factory.createHistoricalRate(coinType, currency, value, timestamp)
    }

    private fun getByHour(coinType: CoinType, currency: String, timestamp: Long): HistoricalRate {
        val providerCoinId = getProviderCoinId(coinType)
        val response = apiManager.getJson(
                "${provider.baseUrl}/data/v2/histohour?api_key=${apiKey}&fsym=${providerCoinId}&tsym=${currency}&limit=1&toTs=$timestamp")
        val value = parseValue(response)

        return factory.createHistoricalRate(coinType, currency, value, timestamp)
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
        //val coin = chartPointKey.coinType
        val providerCoinId = getProviderCoinId(chartPointKey.coinType)

        val currency = chartPointKey.currency
        val chartType = chartPointKey.chartType

        var baseUrl = "${provider.baseUrl}/data/v2/${chartType.resource}?api_key=${apiKey}&fsym=$providerCoinId&tsym=$currency&aggregate=${chartType.interval}"
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
                chartPointKey.coinType,
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
                val json = apiManager.getJson(
                        "${provider.baseUrl}/data/v2/news/?api_key=${apiKey}&categories=${categories}&excludeCategories=Sponsored")
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

    override fun getLatestFiatXRates(sourceCurrency: String, targetCurrency: String): Double {
        val response = apiManager.getJson("${provider.baseUrl}/data/price?api_key=${apiKey}&fsym=${sourceCurrency}" +
                                                  "&tsyms=${targetCurrency}")

        return response.asObject()[targetCurrency].asDouble()
    }
}
