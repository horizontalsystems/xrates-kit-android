package io.horizontalsystems.xrateskit.api.graphprovider

import io.horizontalsystems.xrateskit.api.ApiManager
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.IFiatXRatesProvider
import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Single
import java.math.BigDecimal
import java.util.*
import java.util.logging.Logger

class UniswapGraphProvider(
    private val factory: Factory,
    private val apiManager: ApiManager,
    private val uniswapGraphUrl: String,
    private val fiatXRatesProvider: IFiatXRatesProvider) {

    private val logger = Logger.getLogger("UniswapGraphProvider")
    private val ONE_DAY_SECONDS = 86400 // 1 day in seconds

    private val BASE_FIAT_CURRENCY = "USD"
    private val BASE_COIN_CODE = "ETH"
    private val WETH_TOKEN_CODE = "WETH"
    private val WETH_TOKEN_ADDRESS = "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"


    // Uniswap uses WETH as base. So all requests should be done via WETH.
    // Check if list coinstains ETH, then change it's address to WETH
    private fun tokenAddresses(coins: List<Coin>): List<String>{

        logger.info("Getting XRate from UniswapGraph for:${coins}")
        return coins.mapNotNull {  coin ->

            when (coin.type) {
                is CoinType.Erc20 -> (coin.type as CoinType.Erc20).address.toLowerCase(Locale.getDefault())
                is CoinType.Ethereum -> WETH_TOKEN_ADDRESS
                else -> null
            }
        }
    }

    fun getMarketInfo(coins: List<Coin>, fiatCurrency: String) : Single<List<MarketInfoEntity>>{
        val tokenAddresses = tokenAddresses(coins)

        if(tokenAddresses.isEmpty())
            return Single.just(Collections.emptyList())

        return Single.zip(
            getEthXRate(),
            getXRates(tokenAddresses, System.currentTimeMillis() / 1000 - ONE_DAY_SECONDS),
            getLatestFiatXRates(fiatCurrency),
            {
                    ethXRateResponse, xRatesResponse, ethFiatXRate ->

                val list = mutableListOf<MarketInfoEntity>()
                val ethPrice = ethXRateResponse.rateInUSD * ethFiatXRate

                xRatesResponse.forEach { xRateResponse ->

                    if (xRateResponse.latestRateInETH > 0) {
                        val coinCode = xRateResponse.coinCode.let {
                            if (it.contentEquals(WETH_TOKEN_CODE))
                                BASE_COIN_CODE
                            else
                                it
                        }
                        val coinLatestPrice = xRateResponse.latestRateInETH * ethPrice
                        val coinOpenDayPrice = xRateResponse.dayOpeningRateInUSD * ethFiatXRate
                        val diff = if (coinOpenDayPrice > 0) {
                            ((coinLatestPrice - coinOpenDayPrice) * 100) / coinOpenDayPrice
                        } else 0.0

                        list.add(
                            factory.createMarketInfoEntity(
                                coinCode,
                                fiatCurrency,
                                coinLatestPrice.toBigDecimal(),
                                coinOpenDayPrice.toBigDecimal(),
                                diff.toBigDecimal(),
                                BigDecimal.ZERO,
                                BigDecimal.ZERO,
                                BigDecimal.ZERO
                            )
                        )
                    }
                }

                list
            })
    }

    private fun getEthXRate() : Single<UniswapGraphEthXRateResponse> {

        return Single.create { emitter ->
            try {

                val responseData = apiManager.getJson(uniswapGraphUrl, GraphQueryBuilder.buildETHPriceQuery())

                emitter.onSuccess(UniswapGraphEthXRateResponse.parseData(responseData))

            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    private fun getXRates(tokenAddresses: List<String>, timeStamp: Long) : Single<List<UniswapGraphXRatesResponse>> {

        return Single.create { emitter ->
            try {

                val responseData = apiManager.getJson(
                    uniswapGraphUrl,
                    GraphQueryBuilder.buildHistoricalXRatesQuery(tokenAddresses, timeStamp)
                )

                emitter.onSuccess(UniswapGraphXRatesResponse.parseData(responseData))

            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    private fun getLatestFiatXRates(targetCurrency: String) : Single<Double> {
        return Single.create { emitter ->
            try {
                var fiatRate = 1.0

                if(!targetCurrency.toUpperCase(Locale.getDefault()).contentEquals(BASE_FIAT_CURRENCY)) {
                    fiatRate = fiatXRatesProvider.getLatestFiatXRates(BASE_FIAT_CURRENCY, targetCurrency)
                }

                emitter.onSuccess(fiatRate)

            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }
}

