package io.horizontalsystems.xrateskit.providers.coingecko

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.coins.CoinInfoManager
import io.horizontalsystems.xrateskit.coins.ProviderCoinsManager
import io.horizontalsystems.xrateskit.core.*
import io.horizontalsystems.xrateskit.entities.*
import io.horizontalsystems.xrateskit.providers.InfoProvider
import io.horizontalsystems.xrateskit.utils.RetrofitUtils
import io.reactivex.Single
import java.math.BigDecimal
import java.util.*
import java.util.logging.Logger
import kotlin.math.absoluteValue

class CoinGeckoProvider(
    private val factory: Factory,
    private val coinInfoManager: CoinInfoManager,
    private val providerCoinsManager: ProviderCoinsManager
) : ICoinMarketProvider, IChartInfoProvider, ILatestRateProvider, IHistoricalRateProvider {

    private val logger = Logger.getLogger("CoinGeckoProvider")

    override val provider: InfoProvider = InfoProvider.CoinGecko()
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

    private val coinGeckoService: CoinGeckoService by lazy {
        RetrofitUtils.build(provider.baseUrl).create(CoinGeckoService::class.java)
    }


    init {
        initProvider()
    }

    override fun initProvider() {}
    override fun destroy() {}

    private fun getProviderCoinId(coinType: CoinType): String? {
        return providerCoinsManager.getProviderIds(listOf(coinType), provider).firstOrNull()
    }

    private fun getCoinType(providerCoinId: String): CoinType? {
        return providerCoinsManager.getCoinTypes(providerCoinId.toLowerCase(Locale.ENGLISH), provider).firstOrNull()
    }

    override fun getTopCoinMarketsAsync(currencyCode: String, fetchDiffPeriod: TimePeriod, itemsCount: Int, defiFilter: Boolean): Single<List<CoinMarket>> {
        try {
            var pageNumber = 1
            val singles = mutableListOf<Single<List<CoinMarket>>>()
            var requestItems = itemsCount

            do{
                singles.add(getCoinMarketsSingle(
                    currencyCode = currencyCode,
                    fetchDiffPeriod = fetchDiffPeriod,
                    itemsCount = if(requestItems > MAX_ITEM_PER_PAGE) MAX_ITEM_PER_PAGE else requestItems,
                    pageNumber = pageNumber,
                    defiFilter = defiFilter)
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

    private fun getCoinMarketsSingle( currencyCode: String, fetchDiffPeriod: TimePeriod, itemsCount: Int, pageNumber: Int, defiFilter: Boolean): Single<List<CoinMarket>> {
        return getCoinMarkets(currencyCode,fetchDiffPeriod, itemsCount = itemsCount, pageNumber = pageNumber, defiFilter = defiFilter)
    }

    override fun getCoinMarketsAsync(coinTypes: List<CoinType>, currencyCode: String, fetchDiffPeriod: TimePeriod): Single<List<CoinMarket>> {
        val providerCoinIds = providerCoinsManager.getProviderIds(coinTypes, provider).filterNotNull()

        return when {
            providerCoinIds.isEmpty() -> Single.just(listOf())
            else -> getCoinMarkets(currencyCode,fetchDiffPeriod, coinIds = providerCoinIds, defiFilter = false)
        }
    }

    private fun getCoinMarkets(currencyCode: String, fetchDiffPeriod: TimePeriod, itemsCount: Int? = null, coinIds: List<String>? = null, pageNumber: Int = 1, defiFilter: Boolean): Single<List<CoinMarket>> {
        val priceChangePercentage = when (fetchDiffPeriod) {
            TimePeriod.ALL, TimePeriod.HOUR_24 -> null
            else -> fetchDiffPeriod.title
        }

        val category = if(defiFilter) "decentralized_finance_defi" else null

        return coinGeckoService.coinsMarkets(
            currencyCode,
            coinIds?.joinToString(","),
            category,
            "market_cap_desc",
            itemsCount,
            pageNumber,
            priceChangePercentage
        ).map {
            it.mapNotNull { convertCoinGeckoCoinMarket(it, currencyCode, fetchDiffPeriod) }
        }
    }

    private fun convertCoinGeckoCoinMarket(responseCoinMarket: CoinGeckoService.Response.CoinMarket, currencyCode: String, fetchDiffPeriod: TimePeriod): CoinMarket? {
        val type = getCoinType(responseCoinMarket.id) ?: return null

        val rateDiffPeriod = when (fetchDiffPeriod) {
            TimePeriod.HOUR_1 -> responseCoinMarket.price_change_percentage_1h_in_currency
            TimePeriod.HOUR_24 -> responseCoinMarket.price_change_percentage_24h
            TimePeriod.DAY_7 -> responseCoinMarket.price_change_percentage_7d_in_currency
            TimePeriod.DAY_14 -> responseCoinMarket.price_change_percentage_14d_in_currency
            TimePeriod.DAY_30 -> responseCoinMarket.price_change_percentage_30d_in_currency
            TimePeriod.DAY_200 -> responseCoinMarket.price_change_percentage_200d_in_currency
            TimePeriod.YEAR_1 -> responseCoinMarket.price_change_percentage_1y_in_currency
            else -> null
        } ?: BigDecimal.ZERO

        return factory.createCoinMarket(
            coinData = CoinData(type, responseCoinMarket.symbol.toUpperCase(Locale.ENGLISH), responseCoinMarket.name),
            currency = currencyCode,
            rate = responseCoinMarket.current_price,
            rateOpenDay = responseCoinMarket.price_change_24h,
            rateDiff = responseCoinMarket.price_change_percentage_24h ?: BigDecimal.ZERO,
            volume = responseCoinMarket.total_volume,
            supply = responseCoinMarket.circulating_supply,
            rateDiffPeriod = rateDiffPeriod,
            marketCap = responseCoinMarket.market_cap,
            dilutedMarketCap = responseCoinMarket.fully_diluted_valuation,
            totalSupply = responseCoinMarket.total_supply,
            athChangePercentage = responseCoinMarket.ath_change_percentage,
            atlChangePercentage = responseCoinMarket.atl_change_percentage
        )
    }

    override fun getCoinMarketDetailsAsync(coinType: CoinType, currencyCode: String, rateDiffCoinCodes: List<String>, rateDiffPeriods: List<TimePeriod>): Single<CoinMarketDetails> {
        val providerCoinId = getProviderCoinId(coinType) ?: return Single.error(Exception("No CoinGecko CoinId found for $coinType"))

        return coinGeckoService.coin(providerCoinId, "true", "false", "false")
            .map { coin ->

                val links = mutableMapOf<LinkType, String>()
                coin.links.apply {
                    homepage.firstOrNull()?.let {
                        links[LinkType.WEBSITE] = it
                    }

                    if (!twitter_screen_name.isNullOrBlank()) {
                        links[LinkType.TWITTER] = "https://twitter.com/${twitter_screen_name}"
                    }

                    if (!telegram_channel_identifier.isNullOrBlank()) {
                        links[LinkType.TELEGRAM] = "https://t.me/${telegram_channel_identifier}"
                    }

                    if (!subreddit_url.isNullOrBlank()) {
                        links[LinkType.REDDIT] = subreddit_url
                    }

                    repos_url["github"]?.firstOrNull()?.let {
                        links[LinkType.GITHUB] = it
                    }
                }

                val platforms = coin.platforms.mapNotNull {
                    if (it.value.isBlank()) return@mapNotNull null

                    val platformType = when (it.key.toLowerCase(Locale.ENGLISH)) {
                        "tron" -> CoinPlatformType.TRON
                        "ethereum" -> CoinPlatformType.ETHEREUM
                        "eos" -> CoinPlatformType.EOS
                        "binance-smart-chain" -> CoinPlatformType.BINANCE_SMART_CHAIN
                        "binancecoin" -> CoinPlatformType.BINANCE
                        else -> CoinPlatformType.OTHER
                    }

                    platformType to it.value
                }.toMap()

                val contractAddresses = platforms.map { it.value.toLowerCase(Locale.ENGLISH) }
                val marketTickers = coin.tickers.map {
                    val base = if (contractAddresses.contains(it.base.toLowerCase(Locale.ENGLISH))) {
                        coin.symbol
                    } else {
                        it.base
                    }

                    val target = if (contractAddresses.contains(it.target.toLowerCase(Locale.ENGLISH))) {
                        coin.symbol
                    } else {
                        it.target
                    }

                    CoinGeckoService.Response.Coin.Ticker(base, target, it.market, it.last, it.volume)
                }
                    .filter { filterTicker(it) }
                    .sortedBy { exchangesOrdering[it.market.identifier] ?: Integer.MAX_VALUE }
                    .map {
                        MarketTicker(it.base, it.target, it.market.name, it.last, it.volume)
                    }

                val rateDiffsPeriod = rateDiffPeriods.map<TimePeriod, Pair<TimePeriod, Map<String, BigDecimal>>> { period ->
                    val diffPeriod = when(period) {
                        TimePeriod.HOUR_1 -> coin.market_data.price_change_percentage_1h_in_currency
                        TimePeriod.HOUR_24 -> coin.market_data.price_change_percentage_24h_in_currency
                        TimePeriod.DAY_7 -> coin.market_data.price_change_percentage_7d_in_currency
                        TimePeriod.DAY_14 -> coin.market_data.price_change_percentage_14d_in_currency
                        TimePeriod.DAY_30 -> coin.market_data.price_change_percentage_30d_in_currency
                        TimePeriod.DAY_200 -> coin.market_data.price_change_percentage_200d_in_currency
                        TimePeriod.YEAR_1 -> coin.market_data.price_change_percentage_1y_in_currency
                        else -> coin.market_data.price_change_percentage_24h_in_currency
                    }

                    val rateDiffs = rateDiffCoinCodes.map { coinCode ->
                        coinCode to (diffPeriod[coinCode.toLowerCase(Locale.ENGLISH)] ?: BigDecimal.ZERO)
                    }.toMap()

                    period to rateDiffs
                }.toMap()


                val currencyCodeLowercase = currencyCode.toLowerCase(Locale.ENGLISH)

                CoinMarketDetails(
                    data = CoinData(coinType, coin.symbol, coin.name),
                    currencyCode = currencyCode,
                    rate = coin.market_data.current_price[currencyCodeLowercase] ?: BigDecimal.ZERO,
                    rateHigh24h = coin.market_data.high_24h[currencyCodeLowercase] ?: BigDecimal.ZERO,
                    rateLow24h = coin.market_data.low_24h[currencyCodeLowercase] ?: BigDecimal.ZERO,
                    marketCap = coin.market_data.market_cap[currencyCodeLowercase] ?: BigDecimal.ZERO,
                    marketCapDiff24h = coin.market_data.market_cap_change_percentage_24h,
                    dilutedMarketCap = coin.market_data.fully_diluted_valuation[currencyCodeLowercase] ?: BigDecimal.ZERO,
                    volume24h = coin.market_data.total_volume[currencyCodeLowercase] ?: BigDecimal.ZERO,
                    circulatingSupply = coin.market_data.circulating_supply ?: BigDecimal.ZERO,
                    totalSupply = coin.market_data.total_supply ?: BigDecimal.ZERO,
                    meta = CoinMeta(
                        coin.description["en"] ?: "",
                        coinInfoManager.getLinks(coinType, links),
                        coinInfoManager.getCoinRating(coinType),
                        coinInfoManager.getCoinCategories(coinType),
                        coinInfoManager.getCoinFundCategories(coinType),
                        platforms
                    ),
                    rateDiffs = rateDiffsPeriod,
                    tickers = marketTickers
                )
            }
    }

    private fun filterTicker(ticker: CoinGeckoService.Response.Coin.Ticker) = when {
        ticker.last.compareTo(BigDecimal.ZERO) == 0 -> false
        ticker.volume.compareTo(BigDecimal.ZERO) == 0 -> false
        isSmartContractAddress(ticker.base) -> false
        isSmartContractAddress(ticker.target) -> false
        else -> true
    }

    private fun isSmartContractAddress(v: String): Boolean {
        if (v.length != 42) return false

        return v.matches("^0[xX][A-z0-9]+$".toRegex())
    }

    override fun getChartPointsAsync(chartPointKey: ChartInfoKey): Single<List<ChartPointEntity>> {
        val providerCoinId = getProviderCoinId(chartPointKey.coinType) ?: return Single.error(Exception("No CoinGecko CoinId found for ${chartPointKey.coinType}"))
        return getCoinMarketCharts(providerCoinId, chartPointKey)
    }

    private fun getCoinMarketCharts(providerCoinId: String, chartPointKey: ChartInfoKey): Single<List<ChartPointEntity>> {
        val interval = if (chartPointKey.chartType.days >= 90) "daily" else null

        return coinGeckoService.coinMarketChart(
            providerCoinId,
            chartPointKey.currency,
            2 * chartPointKey.chartType.days,
            interval
        ).map {
            var nextTs = 0L
            val chartPointsCount = chartPointKey.chartType.interval * 2

            it.prices.mapIndexedNotNull { index, rateData ->
                val timestamp = rateData[0].toLong() / 1000

                if (timestamp >= nextTs || it.prices.size <= chartPointsCount) {
                    nextTs = timestamp + chartPointKey.chartType.seconds - 180
                    val rate = rateData[1]
                    val volume = if (chartPointKey.chartType.days >= 90) it.total_volumes[index][1] else BigDecimal.ZERO

                    ChartPointEntity(
                        chartPointKey.chartType,
                        chartPointKey.coinType,
                        chartPointKey.currency,
                        rate,
                        volume,
                        timestamp)

                } else {
                    null
                }
            }
        }
    }

    override fun getLatestRatesAsync(coinTypes: List<CoinType>, currencyCode: String): Single<List<LatestRateEntity>> {
        val coinTypesByCoinGeckoId = mutableMapOf<String, MutableList<CoinType>>()

        coinTypes.forEach { coinType ->
            providerCoinsManager.getProviderId(coinType, provider)?.let { providerCoinId ->
                if (coinTypesByCoinGeckoId[providerCoinId] == null) {
                    coinTypesByCoinGeckoId[providerCoinId] = mutableListOf()
                }

                coinTypesByCoinGeckoId[providerCoinId]?.add(coinType)
            }
        }

        return when {
            coinTypesByCoinGeckoId.isEmpty() -> Single.just(listOf())
            else -> getLatestRates(coinTypesByCoinGeckoId, currencyCode)
        }
    }

    private fun getLatestRates(coinTypesByCoinGeckoId: Map<String, List<CoinType>>, currencyCode: String): Single<List<LatestRateEntity>> {
        val coinIds = coinTypesByCoinGeckoId.keys.toList()

        return coinGeckoService.simplePrice(
            coinIds.joinToString(separator = ","),
            currencyCode,
            "false",
            "false",
            "true",
            "false",
        ).map { simplePrices ->
            val timestamp = System.currentTimeMillis() / 1000
            val currencyCodeLowercase = currencyCode.toLowerCase(Locale.ENGLISH)
            val latestRates = mutableListOf<LatestRateEntity>()

            for (coinId in coinIds) {
                val simplePrice = simplePrices[coinId] ?: continue

                val rate = simplePrice[currencyCodeLowercase] ?: continue
                val rateDiff24h = simplePrice[currencyCodeLowercase + "_24h_change"] ?: continue

                coinTypesByCoinGeckoId[coinId]?.forEach { coinType ->
                    latestRates.add(LatestRateEntity(coinType, currencyCode, rate, rateDiff24h, timestamp))
                }
            }

            latestRates
        }
    }

    override fun getHistoricalRateAsync(coinType: CoinType, currencyCode: String, timestamp: Long): Single<HistoricalRate> {
        val providerCoinId = getProviderCoinId(coinType) ?: return Single.error(Exception("No CoinGecko CoinId found for $coinType"))

        val tsDiff = ((System.currentTimeMillis() / 1000) - timestamp) / 3600 //Diff in hours
        val fromTs = if (tsDiff < 24) timestamp - MINUTES_10_IN_SECONDS else timestamp - HOURS_2_IN_SECONDS
        val toTs = if (tsDiff < 24) timestamp + MINUTES_10_IN_SECONDS else timestamp + HOURS_2_IN_SECONDS

        return coinGeckoService.coinMarketChartRange(
            providerCoinId,
            currencyCode,
            fromTs,
            toTs
        ).map {
            val price = it.prices.minByOrNull {
                val timestampMillis = it[0].toLong()

                (timestampMillis / 1000L - timestamp).absoluteValue
            }!!.get(1)

            HistoricalRate(coinType, currencyCode, price, timestamp)
        }
    }

}
