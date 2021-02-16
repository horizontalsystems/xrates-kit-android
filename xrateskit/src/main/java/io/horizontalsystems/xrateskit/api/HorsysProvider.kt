package io.horizontalsystems.xrateskit.api

import io.horizontalsystems.xrateskit.core.IGlobalCoinMarketProvider
import io.horizontalsystems.xrateskit.entities.GlobalCoinMarket
import io.reactivex.Single
import java.math.BigDecimal

class HorsysProvider(
    private val apiManager: ApiManager
) : IGlobalCoinMarketProvider {
    override val provider: InfoProvider = InfoProvider.HorSys()
    override fun initProvider() {}

    override fun destroy() {}

    override fun getGlobalCoinMarketsAsync(currencyCode: String): Single<GlobalCoinMarket> {
        return Single.create { emitter ->
            try {
                emitter.onSuccess(getGlobalDefiCoinMarkets(currencyCode))

            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }
    }

    private fun getGlobalDefiCoinMarkets(currencyCode: String): GlobalCoinMarket {

        val json = apiManager.getJsonValue("${provider.baseUrl}/markets/global/defi")
        var defiMarketCap: BigDecimal = BigDecimal.ZERO
        var defiMarketCapDiff24h: BigDecimal = BigDecimal.ZERO
        var defiTvl: BigDecimal = BigDecimal.ZERO
        var defiTvlDiff24h: BigDecimal = BigDecimal.ZERO

        json.asObject()?.let { marketData ->
            defiMarketCap = if (marketData.get("marketCap").isNull) BigDecimal.ZERO
            else marketData.get("marketCap").asDouble().toBigDecimal()
            defiMarketCapDiff24h = if (marketData.get("marketCapDiff24h").isNull) BigDecimal.ZERO
            else marketData.get("marketCapDiff24h").asDouble().toBigDecimal()

            defiTvl = if (marketData.get("totalValueLocked").isNull) BigDecimal.ZERO
            else marketData.get("totalValueLocked").asDouble().toBigDecimal()
            defiTvlDiff24h = if (marketData.get("totalValueLockedDiff24h").isNull) BigDecimal.ZERO
            else marketData.get("totalValueLockedDiff24h").asDouble().toBigDecimal()
        }

        return GlobalCoinMarket(
            currencyCode,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            defiMarketCap = defiMarketCap,
            defiMarketCapDiff24h = defiMarketCapDiff24h,
            defiTvl = defiTvl,
            defiTvlDiff24h = defiTvlDiff24h
        )
    }
}