package io.horizontalsystems.xrateskit.api

import io.horizontalsystems.xrateskit.core.ICoinInfoProvider
import io.horizontalsystems.xrateskit.core.IGlobalCoinMarketProvider
import io.horizontalsystems.xrateskit.core.IInfoProvider
import io.horizontalsystems.xrateskit.entities.Coin
import io.horizontalsystems.xrateskit.entities.CoinType
import io.horizontalsystems.xrateskit.entities.GlobalCoinMarket
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import java.util.logging.Logger

class CoinPaprikaProvider(
    private val apiManager: ApiManager
): IGlobalCoinMarketProvider, ICoinInfoProvider {

    override val provider: InfoProvider = InfoProvider.CoinPaprika()
    private val logger = Logger.getLogger("CoinPaprikaProvider")
    private val BTC_ID = "btc-bitcoin"
    private val HOURS_24_IN_SECONDS = 86400

    override fun initProvider() {}
    override fun destroy() {}

    override fun getCoinInfoAsync(platform: CoinType): Single<List<Coin>>{
        val coins = mutableListOf<Coin>()
        val platformId = getCoinId(platform)

        return Single.create { emitter ->
            try {
                val json = apiManager.getJsonValue("${provider.baseUrl}/contracts/${platformId}")

                json.asArray()?.forEach { coinInfo ->
                    coinInfo?.asObject()?.let { element ->
                        if(element.get("active").asBoolean()){
                            val coinId = element.get("id").asString()

                            val splitIndex = if(coinId.indexOf("-") != -1) coinId.indexOf("-") else coinId.length
                            val code = coinId.substring(0, splitIndex)
                            val title = coinId.substring(splitIndex, coinId.length).replace("-", " ").trim()

                            var coinType: CoinType? = null

                            if(platformId.contentEquals("eth-ethereum")){
                                val address = element.get("address").asString()
                                coinType = CoinType.Erc20(address)
                            }

                            coins.add(Coin(code.toUpperCase(), title, coinType))
                        }
                    }
                }

                emitter.onSuccess(coins)

            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

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

    private fun getCoinId(coinType: CoinType): String{
        return when(coinType){
            CoinType.Bitcoin -> "btc-bitcoin"
            CoinType.BitcoinCash -> "bch-bitcoin-cash"
            CoinType.Litecoin -> "ltc-litecoin"
            CoinType.Ethereum -> "eth-ethereum"
            CoinType.Binance -> "bnb-binance-coin"
            CoinType.Eos -> "eos-eos"
            CoinType.Dash -> "dash-dash"
            CoinType.Zcash -> "zec-zcash"
            else -> "eth-ethereum"
        }
    }
}
