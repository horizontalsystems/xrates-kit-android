package io.horizontalsystems.xrateskit.providers.cryptocompare

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.ICryptoNewsProvider
import io.horizontalsystems.xrateskit.core.IFiatXRatesProvider
import io.horizontalsystems.xrateskit.core.IInfoProvider
import io.horizontalsystems.xrateskit.entities.CryptoNews
import io.horizontalsystems.xrateskit.providers.InfoProvider
import io.horizontalsystems.xrateskit.utils.BigDecimalAdapter
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory


class CryptoCompareProvider(
    private val factory: Factory,
    private val apiKey: String
) : IInfoProvider, ICryptoNewsProvider, IFiatXRatesProvider {

    override val provider: InfoProvider = InfoProvider.CryptoCompare()

    private val cryptoCompareService: CryptoCompareService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(provider.baseUrl)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addConverterFactory(
                MoshiConverterFactory
                    .create(
                        Moshi.Builder()
                            .add(BigDecimalAdapter())
                            .addLast(KotlinJsonAdapterFactory())
                            .build()
                    )
            )
            .build()

        retrofit.create(CryptoCompareService::class.java)
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
