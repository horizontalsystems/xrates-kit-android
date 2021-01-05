package io.horizontalsystems.xrateskit.api.graphproviders

import io.horizontalsystems.xrateskit.api.ApiManager
import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.ITopDefiMarketsProvider
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
    private val fiatXRatesProvider: IFiatXRatesProvider)
    : ITopDefiMarketsProvider {

    private val logger = Logger.getLogger("UniswapGraphProvider")
    private val ONE_DAY_SECONDS = 86400 // 1 day in seconds

    private val BASE_FIAT_CURRENCY = "USD"
    private val BASE_COIN_CODE = "ETH"
    private val WETH_TOKEN_CODE = "WETH"
    private val WETH_TOKEN_ADDRESS = "0xc02aaa39b223fe8d0a0e5c4f27ead9083c756cc2"
    private val ethBlocksGraphProvider = EthBlocksGraphProvider(apiManager)


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

    override fun getTopMarketsAsync(currencyCode: String, fetchDiffPeriod: TimePeriod, itemsCount: Int): Single<List<TopMarket>> {

        val periods = mutableMapOf<TimePeriod, Long>()
        periods[TimePeriod.HOUR_24] = (System.currentTimeMillis()/1000) - TimePeriod.HOUR_24.seconds
        periods[fetchDiffPeriod] = (System.currentTimeMillis()/1000) - fetchDiffPeriod.seconds

        return Single.zip(
                ethBlocksGraphProvider.getBlockHeight(periods),
                getTopTokensAsync(itemsCount),
                { blockInfoForPeriod, topTokensResponse ->
                val topMarkets = mutableListOf<TopMarket>()
                val tokensPeriodResponse: UniswapGraphTokensResponse
                var volume24h = BigDecimal.ZERO
                var rateDiff24h = BigDecimal.ZERO
                var rateOpenDay = BigDecimal.ZERO
                var rateDiffPeriod = BigDecimal.ZERO
                val token24hResponse = getTopTokens(itemsCount, blockInfoForPeriod[TimePeriod.HOUR_24])

                logger.info("Completed loading TopDefi tokens:${topTokensResponse.tokens.size}.Fetching market data ... ")

                tokensPeriodResponse =
                    if(fetchDiffPeriod == TimePeriod.HOUR_24)
                        token24hResponse
                    else
                        getTopTokens(itemsCount, blockInfoForPeriod[fetchDiffPeriod])

                logger.info("Completed loading MarketInfo for:${fetchDiffPeriod}.Parsing data ... ")

                topTokensResponse.tokens.forEach { latestTokenInfo ->
                    val latestRate = latestTokenInfo.latestRateInETH * topTokensResponse.ethPriceInUSD

                    token24hResponse.tokens.find { it.tokenAddress.contentEquals(latestTokenInfo.tokenAddress) }?.let {
                        rateOpenDay = it.latestRateInETH * token24hResponse.ethPriceInUSD
                        volume24h = latestTokenInfo.volumeInUSD - it.volumeInUSD

                        val token24hRate = it.latestRateInETH * token24hResponse.ethPriceInUSD
                        rateDiff24h = ((latestRate - token24hRate) * 100.toBigDecimal())/token24hRate
                    }

                    tokensPeriodResponse.tokens.find { it.tokenAddress.contentEquals(latestTokenInfo.tokenAddress) }?.let {
                        val tokenPeriodRate = it.latestRateInETH * token24hResponse.ethPriceInUSD
                        rateDiffPeriod = ((latestRate - tokenPeriodRate) * 100.toBigDecimal())/tokenPeriodRate
                    }

                        topMarkets.add(factory.createTopMarket(
                            Coin(latestTokenInfo.coinCode, latestTokenInfo.coinTitle, CoinType.Erc20(latestTokenInfo.tokenAddress)),
                            currencyCode,
                            rate = latestRate,
                            rateOpenDay = rateOpenDay,
                            rateDiff = rateDiff24h,
                            marketCap = BigDecimal.ZERO,
                            volume = volume24h,
                            supply = BigDecimal.ZERO,
                            liquidity = latestRate * latestTokenInfo.totalLiquidity,
                            rateDiffPeriod = rateDiffPeriod))
                }

                logger.info("Completed parsing data !")

                topMarkets.sortByDescending { it.marketInfo.liquidity }
                topMarkets
            })

    }

    fun getMarketInfo(coins: List<Coin>, fiatCurrency: String) : Single<List<MarketInfoEntity>>{
        val tokenAddresses = tokenAddresses(coins)

        if(tokenAddresses.isEmpty())
            return Single.just(Collections.emptyList())

        return Single.zip(
                getEthXRateAsync(),
                getXRatesAsync(tokenAddresses, System.currentTimeMillis() / 1000 - ONE_DAY_SECONDS),
                getLatestFiatXRatesAsync(fiatCurrency),
                {
                    ethXRateResponse, xRatesResponse, ethFiatXRate ->

                val list = mutableListOf<MarketInfoEntity>()
                val ethPrice = ethXRateResponse.rateInUSD * ethFiatXRate.toBigDecimal()

                xRatesResponse.forEach { xRateResponse ->

                    if (xRateResponse.latestRateInETH > 0) {
                        val coinCode = xRateResponse.coinCode.let {
                            if (it.contentEquals(WETH_TOKEN_CODE))
                                BASE_COIN_CODE
                            else
                                it
                        }
                        val coinLatestPrice = xRateResponse.latestRateInETH * ethPrice.toDouble()
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

    private fun getEthXRateAsync() : Single<UniswapGraphEthXRateResponse> {

        return Single.create { emitter ->
            try {

                val responseData = apiManager.getJson(uniswapGraphUrl, GraphQueryBuilder.buildETHPriceQuery())

                emitter.onSuccess(UniswapGraphEthXRateResponse.parseData(responseData))

            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    private fun getXRatesAsync(tokenAddresses: List<String>, timeStamp: Long) : Single<List<UniswapGraphXRatesResponse>> {

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

    private fun getTopTokensAsync(itemsCount: Int, blockHeight: Long? = null ) : Single<UniswapGraphTokensResponse> {

        return Single.create { emitter ->
            try {
                emitter.onSuccess(getTopTokens(itemsCount, blockHeight))

            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    private fun getTopTokens(itemsCount: Int, blockHeight: Long? = null ) : UniswapGraphTokensResponse {

        val responseData = apiManager.getJson(
                uniswapGraphUrl,
                GraphQueryBuilder.buildTopTokensQuery(itemsCount, blockHeight)
        )

        return UniswapGraphTokensResponse.parseData(responseData)
    }

    private fun getMarketInfo(tokenAddresses: List<String>, blockHeight: Long?) : UniswapGraphTokensResponse {
        val responseData = apiManager.getJson(
            uniswapGraphUrl,
            GraphQueryBuilder.buildMarketInfoQuery(tokenAddresses, blockHeight)
        )

        return UniswapGraphTokensResponse.parseData(responseData)
    }

    private fun getLatestFiatXRatesAsync(targetCurrency: String) : Single<Double> {
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

