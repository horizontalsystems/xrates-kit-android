package io.horizontalsystems.xrateskit.api

import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.ICoinMarketProvider
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Single
import java.math.BigDecimal
import java.util.*
import java.util.logging.Logger

class CoinGeckoProvider(
    private val factory: Factory,
    private val apiManager: ApiManager
) : ICoinMarketProvider {
    private val logger = Logger.getLogger("CoinGeckoProvider")
    private val coinIdsExcluded = listOf("ankreth", "baby-power-index-pool-token", "bifi", "bitcoin-file", "blockidcoin",
                                         "bonded-finance", "bowl-a-coin", "btc-alpha-token", "cactus-finance", "coin-artist",
                                         "compound-coin", "daily-funds", "defi-bids", "defi-nation-signals-dao",
                                         "deipool", "demos", "derogold", "digitalusd", "dipper", "dipper-network",
                                         "dollars", "fin-token", "freetip", "funkeypay", "gdac-token",
                                         "golden-ratio-token", "holy-trinity", "hotnow", "hydro-protocol", "lition",
                                         "master-usd", "memetic", "mir-coin", "morpher", "name-changing-token", "payperex",
                                         "radium", "san-diego-coin", "seed2need", "shardus", "siambitcoin", "socketfinance",
                                         "soft-bitcoin", "spacechain", "stake-coin-2", "stakehound-staked-ether", "super-bitcoin",
                                         "thorchain-erc20", "unicorn-token", "universe-token", "unit-protocol-duck", "usdx-stablecoin",
                                         "usdx-wallet", "wrapped-terra", "yield")

    override val provider: InfoProvider = InfoProvider.CoinGecko()

    init {
        initProvider()
    }

    override fun initProvider() {}

    override fun destroy() {}

    fun getProviderCoinInfoAsync(): Single<List<ProviderCoinInfo>>{
        val providerCoinInfos = mutableListOf<ProviderCoinInfo>()

        return Single.create { emitter ->
            try {
                val json = apiManager.getJsonValue("${provider.baseUrl}/coins/list")

                json.asArray()?.forEach { coinInfo ->
                    coinInfo?.asObject()?.let { element ->

                        val coinId = element.get("id").asString()
                        if(!coinIdsExcluded.contains(coinId)){
                            val coinCode = element.get("symbol").asString().toUpperCase()
                            providerCoinInfos.add(ProviderCoinInfo(provider.id, coinCode, coinId))
                        }
                    }
                }

                emitter.onSuccess(providerCoinInfos)

            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    override fun getTopCoinMarketsAsync(currencyCode: String, fetchDiffPeriod: TimePeriod, itemsCount: Int): Single<List<CoinMarket>> {
        return Single.create { emitter ->
            try {
                emitter.onSuccess(getCoinMarkets(currencyCode,fetchDiffPeriod, itemsCount = itemsCount))

            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }
    }

    override fun getCoinMarketsAsync(coinIds: List<String>, currencyCode: String, fetchDiffPeriod: TimePeriod): Single<List<CoinMarket>> {

        if(coinIds.isEmpty())
            return Single.just(Collections.emptyList())
        
        return Single.create { emitter ->
            try {
                emitter.onSuccess(getCoinMarkets(currencyCode,fetchDiffPeriod, coinIds = coinIds))

            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }
    }

    private fun getCoinMarkets(currencyCode: String, fetchDiffPeriod: TimePeriod, itemsCount: Int? = null, coinIds: List<String>? = null): List<CoinMarket> {

        val topMarkets = mutableListOf<CoinMarket>()
        val coinGeckoMarketsResponses = doCoinMarketsRequest(currencyCode, listOf(fetchDiffPeriod), itemsCount, coinIds)

        coinGeckoMarketsResponses.forEach{ response ->
            if(!coinIdsExcluded.contains(response.coinInfo.coinId)) {
                topMarkets.add(
                    factory.createCoinMarket(
                        coin = Coin(response.coinInfo.coinId, response.coinInfo.title),
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
            }
        }

        return topMarkets
    }

    override fun getCoinMarketDetailsAsync(coinId: String, currencyCode: String, rateDiffCoinCodes: List<String>, rateDiffPeriods: List<TimePeriod>): Single<CoinMarketDetails> {

        return Single.create { emitter ->
            try {

                val coinMarketDetailsResponse = doCoinMarketDetailsRequest(coinId, currencyCode, rateDiffCoinCodes, rateDiffPeriods)

                emitter.onSuccess(CoinMarketDetails(
                    coin = Coin(coinMarketDetailsResponse.coinInfo.coinCode, coinMarketDetailsResponse.coinInfo.title),
                    currencyCode = currencyCode,
                    rate = coinMarketDetailsResponse.coinGeckoMarkets.rate,
                    rateHigh24h = coinMarketDetailsResponse.coinGeckoMarkets.rateHigh24h,
                    rateLow24h = coinMarketDetailsResponse.coinGeckoMarkets.rateLow24h,
                    marketCap = coinMarketDetailsResponse.coinGeckoMarkets.marketCap,
                    marketCapDiff24h = coinMarketDetailsResponse.coinGeckoMarkets.marketCapDiff24h,
                    volume24h = coinMarketDetailsResponse.coinGeckoMarkets.volume24h,
                    circulatingSupply = coinMarketDetailsResponse.coinGeckoMarkets.circulatingSupply,
                    totalSupply = coinMarketDetailsResponse.coinGeckoMarkets.totalSupply,
                    coinInfo = CoinInfo(
                        coinMarketDetailsResponse.coinInfo.description ?: "",
                        coinMarketDetailsResponse.coinInfo.links ?: emptyMap(),
                        null,
                        coinMarketDetailsResponse.coinInfo.platforms),
                    rateDiffs = coinMarketDetailsResponse.rateDiffs
                ))

            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }
    }

    private fun doCoinMarketsRequest(currencyCode: String, fetchDiffPeriods: List<TimePeriod>, itemsCount: Int? = null, coinIds: List<String>? = null): List<CoinGeckoCoinMarketsResponse> {

        val coinIdsParams = if(!coinIds.isNullOrEmpty()) "&ids=${coinIds.joinToString(separator = ",")}"
                            else ""

        val perPage = if(itemsCount != null) "&per_page=${itemsCount}"
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
            "${provider.baseUrl}/coins/${coinId}?tickers=false&localization=false&sparkline=false")

        return CoinGeckoCoinMarketDetailsResponse.parseData(json, currencyCode, rateDiffCoinCodes, rateDiffPeriods)
    }

}