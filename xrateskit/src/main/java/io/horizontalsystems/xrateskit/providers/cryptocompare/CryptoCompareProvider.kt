package io.horizontalsystems.xrateskit.providers.cryptocompare

import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.ICryptoNewsProvider
import io.horizontalsystems.xrateskit.core.IFiatXRatesProvider
import io.horizontalsystems.xrateskit.core.IInfoProvider
import io.horizontalsystems.xrateskit.entities.CryptoNews
import io.horizontalsystems.xrateskit.providers.InfoProvider
import io.horizontalsystems.xrateskit.utils.RetrofitUtils
import io.reactivex.Single


class CryptoCompareProvider(
    private val factory: Factory,
    private val apiKey: String
) : IInfoProvider, ICryptoNewsProvider, IFiatXRatesProvider {

    override val provider: InfoProvider = InfoProvider.CryptoCompare()

    private val cryptoCompareService: CryptoCompareService by lazy {
        RetrofitUtils.build(provider.baseUrl).create(CryptoCompareService::class.java)
    }

    override fun initProvider() {}
    override fun destroy() {}

    //  CryptoNews

    override fun getNews(categories: String): Single<List<CryptoNews>> {
        return cryptoCompareService.news(apiKey, categories, "Sponsored")
            .map {
                it.Data.map {
                    factory.createCryptoNews(
                        it.id,
                        it.published_on,
                        it.imageurl,
                        it.title,
                        it.url,
                        it.body,
                        it.categories.split("|")
                    )
                }
            }
    }

    override fun getLatestFiatXRates(sourceCurrency: String, targetCurrency: String): Single<Double> {
        return cryptoCompareService.price(apiKey, sourceCurrency, targetCurrency)
            .map {
                it[targetCurrency]
            }
    }
}
