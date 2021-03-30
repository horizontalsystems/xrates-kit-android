package io.horizontalsystems.xrateskit.providers.horsys

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import io.horizontalsystems.xrateskit.core.IGlobalCoinMarketProvider
import io.horizontalsystems.xrateskit.entities.GlobalCoinMarket
import io.horizontalsystems.xrateskit.providers.InfoProvider
import io.horizontalsystems.xrateskit.utils.BigDecimalAdapter
import io.reactivex.Single
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory
import java.math.BigDecimal

class HorsysProvider : IGlobalCoinMarketProvider {
    override val provider: InfoProvider = InfoProvider.HorSys()

    private val horsysService: HorsysService by lazy {
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

        retrofit.create(HorsysService::class.java)
    }

    override fun initProvider() {}

    override fun destroy() {}

    override fun getGlobalCoinMarketsAsync(currencyCode: String): Single<GlobalCoinMarket> {
        return horsysService.marketsGlobalDefi()
            .map { globalDefi ->
                GlobalCoinMarket(
                    currencyCode = currencyCode,
                    volume24h = BigDecimal.ZERO,
                    volume24hDiff24h = BigDecimal.ZERO,
                    marketCap = BigDecimal.ZERO,
                    marketCapDiff24h = BigDecimal.ZERO,
                    defiMarketCap = globalDefi.marketCap ?: BigDecimal.ZERO,
                    defiMarketCapDiff24h = globalDefi.marketCapDiff24h ?: BigDecimal.ZERO,
                    defiTvl = globalDefi.totalValueLocked ?: BigDecimal.ZERO,
                    defiTvlDiff24h = globalDefi.totalValueLockedDiff24h ?: BigDecimal.ZERO
                )
            }
    }
}
