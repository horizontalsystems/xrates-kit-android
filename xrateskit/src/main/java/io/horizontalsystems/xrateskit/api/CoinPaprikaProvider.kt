package io.horizontalsystems.xrateskit.api

import io.horizontalsystems.xrateskit.core.IGlobalMarketInfoProvider
import io.horizontalsystems.xrateskit.entities.GlobalMarketInfo
import io.reactivex.Single
import java.util.logging.Logger

class CoinPaprikaProvider(
    private val apiManager: ApiManager
): IGlobalMarketInfoProvider {

    private val logger = Logger.getLogger("CoinPaprikaProvider")
    private val BASE_URL = "https://api.coinpaprika.com/v1"
    private val BTC_ID = "btc-bitcoin"
    private val HOURS_24_IN_SECONDS = 86400

    override fun getGlobalMarketInfoAsync(currencyCode: String): Single<GlobalMarketInfo> {

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

    private fun getMarketOverviewData(currency: String): Single<GlobalMarketInfo> {
        return Single.create { emitter ->
            try {
                val json = apiManager.getJson("$BASE_URL/global")
                val marketInfo = GlobalMarketInfo(
                    currency,
                    json.get("volume_24h_usd").asDouble().toBigDecimal(),
                    json.get("volume_24h_change_24h").asDouble().toBigDecimal(),
                    json.get("market_cap_usd").asDouble().toBigDecimal(),
                    json.get("market_cap_change_24h").asDouble().toBigDecimal(),
                    json.get("bitcoin_dominance_percentage").asDouble().toBigDecimal()
                )
                emitter.onSuccess(marketInfo)

            } catch (ex: Exception) {
                logger.severe(ex.message)
                emitter.onError(ex)
            }
        }
    }

    private fun getMarketCap(coinId: String = BTC_ID, timeStamp: Long): Single<Double> {
        return Single.create { emitter ->
            try {
                val json = apiManager.getJsonValue("$BASE_URL/coins/${coinId}/ohlcv/historical?start=${timeStamp}")
                val marketCap = json.asArray()[0].asObject().get("market_cap").asDouble()

                emitter.onSuccess(marketCap)

            } catch (ex: Exception) {
                logger.severe(ex.message)
                emitter.onError(ex)
            }
        }
    }
}
