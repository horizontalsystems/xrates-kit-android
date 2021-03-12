package io.horizontalsystems.xrateskit.providers

import io.horizontalsystems.xrateskit.api.ApiManager
import io.horizontalsystems.xrateskit.core.*
import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Single

class CryptoCompareProvider(
    private val factory: Factory,
    private val apiKey: String)
    : IInfoProvider, ICryptoNewsProvider, IFiatXRatesProvider {

    override val provider: InfoProvider = InfoProvider.CryptoCompare()
    private val apiManager = ApiManager(provider.rateLimit)

    override fun initProvider() {}
    override fun destroy() {}

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
