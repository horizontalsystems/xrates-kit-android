package io.horizontalsystems.xrateskit.providers

import io.horizontalsystems.xrateskit.api.ApiManager
import io.horizontalsystems.xrateskit.core.IGlobalCoinMarketProvider
import io.horizontalsystems.xrateskit.entities.GlobalCoinMarket
import io.reactivex.Single
import java.util.logging.Logger

class CoinPaprikaProvider(): IGlobalCoinMarketProvider {

    override val provider: InfoProvider = InfoProvider.CoinPaprika()
    private val apiManager = ApiManager(provider.rateLimit)
    private val logger = Logger.getLogger("CoinPaprikaProvider")
    private val BTC_ID = "btc-bitcoin"
    private val HOURS_24_IN_SECONDS = 86400

    override fun initProvider() {}
    override fun destroy() {}


    override fun getGlobalCoinMarketsAsync(currencyCode: String): Single<GlobalCoinMarket> {

        return Single.zip(
            getMarketOverviewData(currencyCode),
            getMarketCap(BTC_ID, (System.currentTimeMillis() / 1000) - HOURS_24_IN_SECONDS),
            { globalMarketInfo, btcMarketCap ->
                val openingMarketCap = (globalMarketInfo.marketCap.multiply(100.toBigDecimal())) / globalMarketInfo.marketCapDiff24h.plus(100.toBigDecimal())
                val openingBtcDominanceDiff = (btcMarketCap * 100) / openingMarketCap.toDouble()
                globalMarketInfo.btcDominanceDiff24h = globalMarketInfo.btcDominance - openingBtcDominanceDiff.toBigDecimal()

                globalMarketInfo
            })
    }

    private fun getMarketOverviewData(currency: String): Single<GlobalCoinMarket> {
        return Single.create { emitter ->
            try {
                val json = apiManager.getJson("${provider.baseUrl}/global")
                val marketInfo = GlobalCoinMarket(
                    currency,
                    json.get("volume_24h_usd").asDouble().toBigDecimal(),
                    json.get("volume_24h_change_24h").asDouble().toBigDecimal(),
                    json.get("market_cap_usd").asDouble().toBigDecimal(),
                    json.get("market_cap_change_24h").asDouble().toBigDecimal(),
                    json.get("bitcoin_dominance_percentage").asDouble().toBigDecimal()
                )
                emitter.onSuccess(marketInfo)

            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }
    }

    private fun getMarketCap(coinId: String = BTC_ID, timeStamp: Long): Single<Double> {
        return Single.create { emitter ->
            try {
                val json = apiManager.getJsonValue("${provider.baseUrl}/coins/${coinId}/ohlcv/historical?start=${timeStamp}")
                val marketCap = json.asArray()[0].asObject().get("market_cap").asDouble()

                emitter.onSuccess(marketCap)

            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }
    }
}
