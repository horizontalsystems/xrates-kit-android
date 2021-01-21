package io.horizontalsystems.xrateskit.api

import io.horizontalsystems.xrateskit.core.Factory
import io.horizontalsystems.xrateskit.core.ICoinMarketProvider
import io.horizontalsystems.xrateskit.core.IGlobalCoinMarketProvider
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.math.BigDecimal
import java.util.logging.Logger

class CoinGeckoProvider(
    private val factory: Factory,
    private val storage: IStorage,
    private val apiManager: ApiManager
) : ICoinMarketProvider, IGlobalCoinMarketProvider {
    private val logger = Logger.getLogger("CoinGeckoProvider")

    override val provider: InfoProvider = InfoProvider.CoinGecko()

    private var providerDisposables = CompositeDisposable()

    init {
        initProvider()
    }

    override fun initProvider() {

        if(storage.getProviderCoinsInfoCount(provider.id) < 1){
            getProviderCoinInfoAsync()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({ coinInfos ->
                               storage.saveProviderCoinInfo(coinInfos)
                           }, {
                           })
                .let {
                    providerDisposables.add(it)
                }
        }
    }

    override fun destroy() {
        providerDisposables.clear()
    }

    private fun getProviderCoinInfoAsync(): Single<List<ProviderCoinInfo>>{
        val providerCoinInfos = mutableListOf<ProviderCoinInfo>()

        return Single.create { emitter ->
            try {
                val json = apiManager.getJsonValue("${provider.baseUrl}/coins/list")

                json.asArray()?.forEach { coinInfo ->
                    coinInfo?.asObject()?.let { element ->
                        val coinId = element.get("id").asString()
                        val coinCode = element.get("symbol").asString().toUpperCase()
                        providerCoinInfos.add(ProviderCoinInfo(provider.id, coinCode, coinId))
                    }
                }

                emitter.onSuccess(providerCoinInfos)

            } catch (e: Exception) {
                emitter.onError(e)
            }
        }
    }

    override fun getGlobalCoinMarketsAsync(currencyCode: String): Single<GlobalCoinMarket> {
        return Single.create { emitter ->
            try {
                emitter.onSuccess(getGlobalDefiCoinMarkets(currencyCode))

            } catch (ex: Exception) {
                emitter.onError(ex)
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

    override fun getCoinMarketsAsync(coins: List<Coin>, currencyCode: String, fetchDiffPeriod: TimePeriod): Single<List<CoinMarket>> {
        return Single.create { emitter ->
            try {
                emitter.onSuccess(getCoinMarkets(currencyCode,fetchDiffPeriod, coins = coins))

            } catch (ex: Exception) {
                emitter.onError(ex)
            }
        }
    }

    private fun getCoinIds(coins: List<Coin>? = null): String{
        coins?.let {
            val coinCodes = it.map { coin -> coin.code }
            val coinInfos = storage.getProviderCoinInfoByCodes(provider.id, coinCodes)

            val coinIds = coinInfos.map { coinInfo -> coinInfo.providerCoinId }
            return "&ids=${coinIds.joinToString(separator = ",")}"
        }

        return ""
    }

    private fun getGlobalDefiCoinMarkets(currencyCode: String): GlobalCoinMarket {

        val json = apiManager.getJsonValue("${provider.baseUrl}/global/decentralized_finance_defi")
        var defiMarketCap: BigDecimal = BigDecimal.ZERO
        json.asObject()?.let { marketData ->
            marketData.get("data")?.asObject()?.let {
                defiMarketCap = if ( it.get("defi_market_cap").isNull) BigDecimal.ZERO
                else it.get("defi_market_cap").asString().toBigDecimal()
            }
        }

        return GlobalCoinMarket(currencyCode, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, defiMarketCap = defiMarketCap)
    }


    private fun getCoinMarkets(currencyCode: String, fetchDiffPeriod: TimePeriod, itemsCount: Int? = null, coins: List<Coin>? = null): List<CoinMarket> {

        val coinIds = getCoinIds(coins)
        val perPage = if(itemsCount != null) "&per_page=${itemsCount}" else ""

        val priceChangePercentage =
                if (fetchDiffPeriod != TimePeriod.HOUR_24 && fetchDiffPeriod != TimePeriod.DAY_START)
                    "&price_change_percentage=${fetchDiffPeriod.title}"
                else ""

        val json = apiManager.getJsonValue(
                "${provider.baseUrl}/coins/markets?${coinIds}&vs_currency=${currencyCode}${priceChangePercentage}&order=market_cap_desc${perPage}")
        val topMarkets = mutableListOf<CoinMarket>()
        json.asArray()?.forEach { marketData ->
            marketData?.asObject()?.let { element ->
                val coinCode = element.get("symbol").asString().toUpperCase()
                val title = element.get("name").asString()

                val rate = if (element.get("current_price").isNull) BigDecimal.ZERO
                else element.get("current_price").asDouble().toBigDecimal()

                val rateOpenDay = if (element.get("price_change_24h").isNull) BigDecimal.ZERO
                else rate + element.get("price_change_24h").asDouble().toBigDecimal()

                val supply = if (element.get("circulating_supply").isNull) BigDecimal.ZERO
                else element.get("circulating_supply").asDouble().toBigDecimal()

                val volume = if (element.get("total_volume").isNull) BigDecimal.ZERO
                else element.get("total_volume").asDouble().toBigDecimal()
                val marketCap = if (element.get("market_cap").isNull) BigDecimal.ZERO
                else element.get("market_cap").asDouble().toBigDecimal()

                val priceDiffFieldName =
                        when (fetchDiffPeriod) {
                            TimePeriod.DAY_7 -> "price_change_percentage_7d_in_currency"
                            TimePeriod.HOUR_1 -> "price_change_percentage_1h_in_currency"
                            TimePeriod.HOUR_24 -> "price_change_percentage_24h"
                            TimePeriod.DAY_30 -> "price_change_percentage_30d_in_currency"
                            TimePeriod.YEAR_1 -> "price_change_percentage_1y_in_currency"
                            else -> "price_change_percentage_24h"
                        }

                val rateDiffPeriod =
                        if (element.get(priceDiffFieldName).isNull) BigDecimal.ZERO
                        else element.get(priceDiffFieldName).asDouble().toBigDecimal()

                val rateDiff24h =
                        if (element.get("price_change_percentage_24h").isNull) BigDecimal.ZERO
                        else element.get("price_change_percentage_24h").asDouble().toBigDecimal()

                topMarkets.add(
                    factory.createCoinMarket(
                        coin = Coin(coinCode, title),
                        currency = currencyCode,
                        rate = rate,
                        rateOpenDay = rateOpenDay,
                        rateDiff = rateDiff24h,
                        volume = volume,
                        supply = supply,
                        rateDiffPeriod = rateDiffPeriod,
                        marketCap = marketCap
                    )
                )
            }
        }

        return topMarkets
    }
}