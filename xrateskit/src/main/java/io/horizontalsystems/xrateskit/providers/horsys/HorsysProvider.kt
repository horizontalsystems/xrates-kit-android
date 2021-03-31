package io.horizontalsystems.xrateskit.providers.horsys

import io.horizontalsystems.xrateskit.core.IGlobalCoinMarketProvider
import io.horizontalsystems.xrateskit.entities.GlobalCoinMarket
import io.horizontalsystems.xrateskit.providers.InfoProvider
import io.horizontalsystems.xrateskit.utils.RetrofitUtils
import io.reactivex.Single
import java.math.BigDecimal

class HorsysProvider : IGlobalCoinMarketProvider {
    override val provider: InfoProvider = InfoProvider.HorSys()

    private val horsysService: HorsysService by lazy {
        RetrofitUtils.build(provider.baseUrl).create(HorsysService::class.java)
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
