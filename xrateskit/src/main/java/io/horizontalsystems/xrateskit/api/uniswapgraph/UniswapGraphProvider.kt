package io.horizontalsystems.xrateskit.api.uniswapgraph

import com.eclipsesource.json.JsonObject
import io.horizontalsystems.xrateskit.api.ApiManager
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.IFiatXRatesProvider
import io.horizontalsystems.xrateskit.entities.Coin
import io.horizontalsystems.xrateskit.entities.CoinType
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
    private val ONE_DAY_SECONDS = 86400 // 1 day in seconds

    private val BASE_FIAT_CURRENCY = "USD"
    private val BASE_COIN_CODE = "ETH"
    private val WETH_TOKEN_CODE = "WETH"
    private val WETH_TOKEN_ADDRESS = "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"

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

    // Uniswap uses WETH as base. So all requests should be done via WETH.
    // Check if list coinstains ETH, then change it's address to WETH
    private fun prepareInputParams(coins: List<Coin>){
        coins.find { coin -> coin.code.equals(BASE_COIN_CODE) }?.let {
                it.type = CoinType.Erc20(WETH_TOKEN_ADDRESS)
        }
    }

    private fun getLatestXRates(coins: List<Coin>, fiatCurrency: String) : List<MarketInfoEntity> {

        try {

            prepareInputParams(coins)

            val list = mutableListOf<MarketInfoEntity>()
            var fiatRate = 1.0

            logger.info("Getting XRates from Uniswap for Coins:${coins}")
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

            if (!fiatCurrency.toUpperCase(Locale.getDefault()).contentEquals(BASE_FIAT_CURRENCY)) {
                fiatRate = fiatXRatesProvider.getLatestFiatXRates(BASE_FIAT_CURRENCY, fiatCurrency)
                ethPrice *= fiatRate
            }

            val tokensData = dataLatestXRates.get("tokens").asArray()

            for (coinData in tokensData) {
                try {

                    val coin = coinData.asObject()
                    val coinCode = coin["symbol"].asString()?.let {
                            if(it.contentEquals(WETH_TOKEN_CODE))
                                BASE_COIN_CODE
                            else
                                coin["symbol"].asString()
                    }
                    val coinEthRate = coin["derivedETH"].asString().toDouble()
                    val coinLatestPrice = coinEthRate * ethPrice
                    val coinOpenDayUSDPrice =
                        dataHistoXRates[coinCode].asArray()[0].asObject()["priceUSD"].asString().toDouble()
                    val coinOpenDayPrice = fiatRate * coinOpenDayUSDPrice
                    val diff = ((coinLatestPrice - coinOpenDayPrice) * 100) / coinOpenDayPrice

                    coinCode?.let {
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
                    }

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

