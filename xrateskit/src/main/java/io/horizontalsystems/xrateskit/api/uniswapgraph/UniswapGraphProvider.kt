package io.horizontalsystems.xrateskit.api.uniswapgraph

import com.eclipsesource.json.JsonObject
import io.horizontalsystems.xrateskit.api.ApiManager
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.IFiatXRatesProvider
import io.horizontalsystems.xrateskit.entities.Coin
import io.horizontalsystems.xrateskit.entities.MarketInfoEntity
import io.reactivex.Single
import java.util.*
import java.util.logging.Logger

class UniswapGraphProvider(
    private val factory: Factory,
    private val apiManager: ApiManager,
    private val fiatXRatesProvider: IFiatXRatesProvider) {

    private val logger = Logger.getLogger("UniswapGraphProvider")
    private val GRAPH_NODE_URL = "https://api.thegraph.com/subgraphs/name/uniswap/uniswap-v2"
    private val BASE_FIAT_CURRENCY = "USD"
    private val ONE_DAY_SECONDS = 86400 // 1 day in seconds

    fun getMarketInfo(coins: List<Coin>, fiatCurrency: String) : Single<List<MarketInfoEntity>> {

        if(coins.isEmpty())
            return Single.just(Collections.emptyList())

        return Single.create { emitter ->
            try {
                emitter.onSuccess(getLatestXRates(coins, fiatCurrency))
            } catch (e: Exception) {
                logger.severe(e.message)
                emitter.onError(e)
            }
        }
    }

    private fun getLatestXRates(coins: List<Coin>, fiatCurrency: String) : List<MarketInfoEntity> {

        try {

            val list = mutableListOf<MarketInfoEntity>()
            var fiatRate = 1.0

            val jsonLatestXRates = apiManager.getJson(
                GRAPH_NODE_URL,
                GraphQueryBuilder.buildLatestXRatesQuery(coins)
            )

            val jsonETHPrice = apiManager.getJson(
                GRAPH_NODE_URL,
                GraphQueryBuilder.buildETHPriceQuery()
            )

            val jsonHistoXRates = getHistoricalXRates(coins, "", System.currentTimeMillis()/1000 - ONE_DAY_SECONDS)
            val dataLatestXRates = jsonLatestXRates.get("data").asObject()
            val dataHistoXRates = jsonHistoXRates.get("data").asObject()
            var ethPrice = jsonETHPrice.get("data").asObject().get("bundle").let {
                it.asObject()["ethPriceUSD"].asString().toDouble()
            }

            if (!fiatCurrency.toUpperCase().contentEquals(BASE_FIAT_CURRENCY)) {
                fiatRate = fiatXRatesProvider.getLatestFiatXRates(BASE_FIAT_CURRENCY, fiatCurrency)
                ethPrice *= fiatRate
            }

            val tokensData = dataLatestXRates.get("tokens").asArray()

            for (coinData in tokensData) {
                try {

                    val coin = coinData.asObject()
                    val coinCode = if(coin["symbol"].asString().contentEquals("WETH")) "ETH"
                                   else coin["symbol"].asString()
                    val coinEthRate = coin["derivedETH"].asString().toDouble()

                    val coinLatestPrice = coinEthRate * ethPrice
                    val coinOpenDayUSDPrice =
                        dataHistoXRates[coinCode].asArray()[0].asObject()["priceUSD"].asString().toDouble()
                    val coinOpenDayPrice = fiatRate * coinOpenDayUSDPrice
                    val diff = ((coinLatestPrice - coinOpenDayPrice) * 100) / coinOpenDayPrice

                    list.add(
                        factory.createMarketInfoEntity(
                            coinCode,
                            fiatCurrency,
                            coinLatestPrice.toBigDecimal(),
                            coinOpenDayPrice.toBigDecimal(),
                            diff.toBigDecimal(),
                            0.0,
                            0.0,
                            0.0
                        )
                    )
                } catch (e: Exception) {
                    print(e)
                    continue
                }

                return list
            }
        } catch (e: java.lang.Exception){
            logger.severe("Error collecting XRates for ETH/Erc20 tokens: ${e.message}")
        }

        return Collections.emptyList<MarketInfoEntity>()
    }

    private fun getHistoricalXRates(coins: List<Coin>, fiatCurrency: String, timeStamp: Long) : JsonObject {
        return apiManager.getJson(GRAPH_NODE_URL,
                                  GraphQueryBuilder.buildHistoricalXRatesQuery(coins, timeStamp)
        )
    }
}

