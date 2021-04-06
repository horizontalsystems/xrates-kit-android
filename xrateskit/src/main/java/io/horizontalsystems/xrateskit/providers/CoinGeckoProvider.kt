package io.horizontalsystems.xrateskit.providers

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.api.ApiManager
import io.horizontalsystems.xrateskit.coins.CoinInfoManager
import io.horizontalsystems.xrateskit.coins.ProviderCoinError
import io.horizontalsystems.xrateskit.coins.ProviderCoinsManager
import io.horizontalsystems.xrateskit.core.*
import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Single
import java.math.BigDecimal
import java.util.*
import java.util.logging.Logger

class CoinGeckoProvider(
    private val factory: Factory,
    private val coinInfoManager: CoinInfoManager,
    private val providerCoinsManager: ProviderCoinsManager
) : ICoinMarketProvider, IChartInfoProvider, ILatestRateProvider, IHistoricalRateProvider {

    private val logger = Logger.getLogger("CoinGeckoProvider")

    override val provider: InfoProvider = InfoProvider.CoinGecko()
    private val apiManager = ApiManager.create(provider.rateLimit)
    private val MAX_ITEM_PER_PAGE = 250
    private val MINUTES_10_IN_SECONDS = 60 * 10
    private val HOURS_2_IN_SECONDS = 60 * 60 * 2
    private val exchangesOrdering by lazy {
        var i = 0

        hashMapOf(
            "binance" to ++i,
            "binance_us" to ++i,
            "binance_dex" to ++i,
            "binance_dex_mini" to ++i,
            "uniswap_v1" to ++i,
            "uniswap" to ++i,
            "gdax" to ++i, // Coinbase
            "sushiswap" to ++i,
            "huobi" to ++i,
            "huobi_thailand" to ++i,
            "huobi_id" to ++i,
            "huobi_korea" to ++i,
            "huobi_japan" to ++i,
            "ftx_spot" to ++i,
            "ftx_us" to ++i,
            "one_inch" to ++i,
            "one_inch_liquidity_protocol" to ++i,
            "one_inch_liquidity_protocol_bsc" to ++i,
        )
    }

    init {
        initProvider()
    }

    override fun initProvider() {}
    override fun destroy() {}

    private fun getProviderCoinId(coinType: CoinType): String {
        providerCoinsManager.getProviderIds(listOf(coinType), this.provider).let {
            if(it.isNotEmpty()){
                it[0]?.let {
                    return it
                }
            }
        }

        logger.warning(" *** Error! Cannot get providerCoin for CoinType:${coinType.ID}")
        throw ProviderCoinError.NoMatchingExternalId()
    }

    private fun getCoinType(providerCoinId: String?): CoinType {

        providerCoinId?.let {
            providerCoinsManager.getCoinTypes(it.toLowerCase(), this.provider).let { coinTypes ->
                if (coinTypes.isNotEmpty()) {
                    return coinTypes[0]
                }
            }
        }

        logger.warning(" *** Error! Cannot get coinType for providerCoin:${providerCoinId}")
        throw ProviderCoinError.NoMatchingExternalId()
    }

    override fun getTopCoinMarketsAsync(currencyCode: String, fetchDiffPeriod: TimePeriod, itemsCount: Int): Single<List<CoinMarket>> {
        try {
            var pageNumber = 1
            val singles = mutableListOf<Single<List<CoinMarket>>>()
            var requestItems = itemsCount

            do{
                singles.add(getCoinMarketsSingle(
                    currencyCode = currencyCode,
                    fetchDiffPeriod = fetchDiffPeriod,
                    itemsCount = if(requestItems > MAX_ITEM_PER_PAGE) MAX_ITEM_PER_PAGE else requestItems,
                    pageNumber = pageNumber)
                )

                requestItems -= MAX_ITEM_PER_PAGE
                pageNumber ++

            } while (requestItems > 0)


            return Single.zip(singles) { zippedObject ->
                zippedObject.toList().flatMap { it as List<CoinMarket>}
            }

        } catch (ex: Exception) {
            logger.warning(ex.localizedMessage)
        }

        return Single.just(emptyList())
    }

    private fun getCoinMarketsSingle( currencyCode: String, fetchDiffPeriod: TimePeriod, itemsCount: Int, pageNumber: Int): Single<List<CoinMarket>> {
        return Single.create { emitter->
            emitter.onSuccess(getCoinMarkets(currencyCode,fetchDiffPeriod, itemsCount = itemsCount, pageNumber = pageNumber))
        }
    }

    override fun getCoinMarketsAsync(coinTypes: List<CoinType>, currencyCode: String, fetchDiffPeriod: TimePeriod): Single<List<CoinMarket>> {
        return Single.create { emitter ->
            try {
                val providerCoinIds = providerCoinsManager.getProviderIds(coinTypes, this.provider).mapNotNull { it }
                if(providerCoinIds.isEmpty())
                    emitter.onSuccess(Collections.emptyList())
                else
                    emitter.onSuccess(getCoinMarkets(currencyCode,fetchDiffPeriod, coinIds = providerCoinIds))

            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }
    }

    private fun getCoinMarkets(currencyCode: String, fetchDiffPeriod: TimePeriod, itemsCount: Int? = null, coinIds: List<String>? = null, pageNumber: Int = 1): List<CoinMarket> {

        val topMarkets = mutableListOf<CoinMarket>()
        val coinGeckoMarketsResponses = doCoinMarketsRequest(currencyCode, listOf(fetchDiffPeriod), itemsCount, coinIds, pageNumber)
        var coinId = ""

        coinGeckoMarketsResponses.forEach { response ->

            try{
                coinId = response.coinInfo.coinId

                topMarkets.add(
                    factory.createCoinMarket(
                        coinData = CoinData(
                            getCoinType(response.coinInfo.coinId),
                            response.coinInfo.coinCode,
                            response.coinInfo.title
                        ),
                        currency = currencyCode,
                        rate = response.coinGeckoMarkets.rate,
                        rateOpenDay = response.coinGeckoMarkets.rateOpenDay,
                        rateDiff = response.coinGeckoMarkets.rateDiffPeriod?.get(TimePeriod.HOUR_24) ?: BigDecimal.ZERO,
                        volume = response.coinGeckoMarkets.volume24h,
                        supply = response.coinGeckoMarkets.circulatingSupply,
                        rateDiffPeriod = response.coinGeckoMarkets.rateDiffPeriod?.get(fetchDiffPeriod) ?: BigDecimal.ZERO,
                        marketCap = response.coinGeckoMarkets.marketCap
                    )
                )
            } catch(e: ProviderCoinError.NoMatchingExternalId){
                println(" No provider record for CoinId: ${coinId}")
            }
        }

        return topMarkets
    }

    override fun getCoinMarketDetailsAsync(coinType: CoinType, currencyCode: String, rateDiffCoinCodes: List<String>, rateDiffPeriods: List<TimePeriod>): Single<CoinMarketDetails> {

        return Single.create { emitter ->
            try {

                val providerCoinId = getProviderCoinId(coinType)
                val coinMarketDetailsResponse =
                    doCoinMarketDetailsRequest(providerCoinId, currencyCode, rateDiffCoinCodes, rateDiffPeriods)
                val coinRating = coinInfoManager.getCoinRating(coinType)
                val categories = coinInfoManager.getCoinCategories(coinType)
                val funds = coinInfoManager.getCoinFundCategories(coinType)
                val links = coinInfoManager.getLinks(coinType, coinMarketDetailsResponse.coinInfo.links)

                emitter.onSuccess(
                    CoinMarketDetails(
                        data = CoinData(
                            coinType,
                            coinMarketDetailsResponse.coinInfo.coinCode,
                            coinMarketDetailsResponse.coinInfo.title
                        ),
                        currencyCode = currencyCode,
                        rate = coinMarketDetailsResponse.coinGeckoMarkets.rate,
                        rateHigh24h = coinMarketDetailsResponse.coinGeckoMarkets.rateHigh24h,
                        rateLow24h = coinMarketDetailsResponse.coinGeckoMarkets.rateLow24h,
                        marketCap = coinMarketDetailsResponse.coinGeckoMarkets.marketCap,
                        marketCapDiff24h = coinMarketDetailsResponse.coinGeckoMarkets.marketCapDiff24h,
                        volume24h = coinMarketDetailsResponse.coinGeckoMarkets.volume24h,
                        circulatingSupply = coinMarketDetailsResponse.coinGeckoMarkets.circulatingSupply,
                        totalSupply = coinMarketDetailsResponse.coinGeckoMarkets.totalSupply,
                        meta = CoinMeta(
                            coinMarketDetailsResponse.coinInfo.description ?: "",
                            links,
                            coinRating,
                            categories,
                            funds,
                            coinMarketDetailsResponse.coinInfo.platforms
                        ),

                        rateDiffs = coinMarketDetailsResponse.rateDiffs,
                        tickers = coinMarketDetailsResponse.coinInfo.tickers
                            .filter { filterTicker(it) }
                            .sortedBy { exchangesOrdering[it.marketId] ?: Integer.MAX_VALUE }
                            .map {
                                MarketTicker(it.base, it.target, it.marketName, it.rate, it.volume)
                            }
                    )
                )

            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }
    }

    private fun filterTicker(it: CoinGeckoTickersResponse) = when {
        it.rate.compareTo(BigDecimal.ZERO) == 0 -> false
        it.volume.compareTo(BigDecimal.ZERO) == 0 -> false
        isSmartContractAddress(it.base) -> false
        isSmartContractAddress(it.target) -> false
        else -> true
    }

    private fun isSmartContractAddress(v: String): Boolean {
        if (v.length != 42) return false

        return v.matches("^0[xX][A-z0-9]+$".toRegex())
    }

    private fun doCoinMarketsRequest(currencyCode: String, fetchDiffPeriods: List<TimePeriod>, itemsCount: Int? = null, coinIds: List<String>? = null, pageNumber: Int = 1): List<CoinGeckoCoinMarketsResponse> {

        val coinIdsParams = if(!coinIds.isNullOrEmpty()) "&ids=${coinIds.joinToString(separator = ",")}"
        else ""

        val perPage = if(itemsCount != null) "&page=${pageNumber}&per_page=${itemsCount}"
        else ""

        val priceChangePercentage = fetchDiffPeriods.map { period ->
            if(period != TimePeriod.ALL && period != TimePeriod.HOUR_24) period.title
            else null
        }.filterNotNull().let { element ->
            if(element.isEmpty()) ""
            else "&price_change_percentage=${element.joinToString(",")}"
        }

        val json = apiManager.getJsonValue(
            "${provider.baseUrl}/coins/markets?${coinIdsParams}&vs_currency=${currencyCode}${priceChangePercentage}&order=market_cap_desc${perPage}")

        return CoinGeckoCoinMarketsResponse.parseData(json)
    }

    private fun doCoinMarketDetailsRequest(coinId: String, currencyCode: String, rateDiffCoinCodes: List<String>, rateDiffPeriods: List<TimePeriod>): CoinGeckoCoinMarketDetailsResponse {

        val json = apiManager.getJsonValue(
            "${provider.baseUrl}/coins/${coinId}?tickers=true&localization=false&sparkline=false")

        return CoinGeckoCoinMarketDetailsResponse.parseData(json, currencyCode, rateDiffCoinCodes, rateDiffPeriods)
    }

    override fun getChartPointsAsync(chartPointKey: ChartInfoKey): Single<List<ChartPointEntity>> {

        return Single.create { emitter ->
            try {
                val providerCoinId = getProviderCoinId(chartPointKey.coinType)
                emitter.onSuccess(getCoinMarketCharts(providerCoinId, chartPointKey))

            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }
    }

    private fun getCoinMarketCharts(providerCoinId: String, chartPointKey: ChartInfoKey): List<ChartPointEntity> {

        val interval = if(chartPointKey.chartType.days >= 90) "&interval=daily" else ""
        val json = apiManager.getJsonValue(
            "${provider.baseUrl}/coins/${providerCoinId}/market_chart?vs_currency=${chartPointKey.currency}&days=${2 * chartPointKey.chartType.days}${interval}")

        return CoinGeckoMarketChartsResponse.parseData(chartPointKey, json).map { response ->
            ChartPointEntity(
                chartPointKey.chartType,
                chartPointKey.coinType,
                chartPointKey.currency,
                response.rate,
                response.volume,
                response.timestamp)
        }
    }

    override fun getLatestRatesAsync(coinTypes: List<CoinType>, currencyCode: String): Single<List<LatestRateEntity>> {

        return Single.create { emitter ->
            try {
                val coinTypesByCoinGeckoId = mutableMapOf<String, MutableList<CoinType>>()

                coinTypes.forEach { coinType ->
                    providerCoinsManager.getProviderId(coinType, provider)?.let { providerCoinId ->
                        if (coinTypesByCoinGeckoId[providerCoinId] == null) {
                            coinTypesByCoinGeckoId[providerCoinId] = mutableListOf()
                        }

                        coinTypesByCoinGeckoId[providerCoinId]?.add(coinType)
                    }
                }

                if (coinTypesByCoinGeckoId.isEmpty())
                    emitter.onSuccess(Collections.emptyList())
                else
                    emitter.onSuccess(getLatestRates(coinTypesByCoinGeckoId, currencyCode))

            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }
    }

    private fun getLatestRates(coinTypesByCoinGeckoId: Map<String, List<CoinType>>, currencyCode: String): List<LatestRateEntity>{
        val coinIds = coinTypesByCoinGeckoId.keys.toList()
        val latestRates = mutableListOf<LatestRateEntity>()
        val coinIdsParams = "&ids=${coinIds.joinToString(separator = ",")}"

        val json = apiManager.getJsonValue(
            "${provider.baseUrl}/simple/price?${coinIdsParams}" +
                    "&vs_currencies=${currencyCode}&include_market_cap=false" +
                    "&include_24hr_vol=false&include_24hr_change=true&include_last_updated_at=false")

        val responses = CoinGeckoCoinPriceResponse.parseData(json, currencyCode, coinIds)
        val timestamp = System.currentTimeMillis() / 1000
        responses.forEach {
            val providerCoinId = it.coinId
            coinTypesByCoinGeckoId[providerCoinId]?.forEach { coinType ->
                latestRates.add(
                    LatestRateEntity(
                        coinType = coinType,
                        currencyCode = currencyCode,
                        rateDiff24h = it.rateDiff24h,
                        rate = it.rate,
                        timestamp = timestamp
                    )
                )
            }
        }

        return latestRates
    }

    override fun getHistoricalRateAsync(coinType: CoinType, currencyCode: String, timestamp: Long): Single<HistoricalRate> {

        return Single.create { emitter ->
            try {
                val providerCoinId = getProviderCoinId(coinType)
                emitter.onSuccess(getHistoricalRate(coinType, providerCoinId, currencyCode, timestamp))

            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }
    }

    private fun getHistoricalRate(coinType: CoinType, providerCoinId: String, currencyCode: String, timestamp: Long): HistoricalRate {

        val tsDiff = ((System.currentTimeMillis()/1000) - timestamp)/ 3600 //Diff in hours
        val fromTs = if(tsDiff < 24) timestamp - MINUTES_10_IN_SECONDS else timestamp - HOURS_2_IN_SECONDS
        val toTs = if(tsDiff < 24) timestamp + MINUTES_10_IN_SECONDS else timestamp + HOURS_2_IN_SECONDS

        val json = apiManager.getJsonValue(
            "${provider.baseUrl}/coins/${providerCoinId}/market_chart/range?vs_currency=${currencyCode}&from=${fromTs}&to=${toTs}")

        // Get the closest value for timestamp
        val response = CoinGeckoHistoRateResponse.parseData(json, timestamp)
        val histoRate = response.minByOrNull { it.timeDiff }?.rate ?: BigDecimal.ZERO

        return HistoricalRate(
            coinType = coinType,
            currencyCode = currencyCode,
            timestamp = timestamp,
            value = histoRate
        )
    }
}