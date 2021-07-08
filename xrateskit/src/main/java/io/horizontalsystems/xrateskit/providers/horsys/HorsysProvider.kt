package io.horizontalsystems.xrateskit.providers.horsys

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.coins.ProviderCoinsManager
import io.horizontalsystems.xrateskit.core.IDefiMarketsProvider
import io.horizontalsystems.xrateskit.core.IGlobalCoinMarketProvider
import io.horizontalsystems.xrateskit.core.ITokenInfoProvider
import io.horizontalsystems.xrateskit.entities.*
import io.horizontalsystems.xrateskit.providers.InfoProvider
import io.horizontalsystems.xrateskit.utils.RetrofitUtils
import io.reactivex.Single
import java.math.BigDecimal
import java.util.*

class HorsysProvider(
    private val providerCoinsManager: ProviderCoinsManager
) : IGlobalCoinMarketProvider, IDefiMarketsProvider, ITokenInfoProvider {

    override val provider: InfoProvider = InfoProvider.HorSys()

    private val horsysService: HorsysService by lazy {
        RetrofitUtils.build(provider.baseUrl).create(HorsysService::class.java)
    }

    override fun initProvider() {}

    override fun destroy() {}

    private fun getCoinType(providerCoinId: String, provider: InfoProvider): CoinType? {
        return providerCoinsManager.getCoinTypes(providerCoinId.toLowerCase(Locale.ENGLISH), provider).firstOrNull()
    }

    override fun getTopTokenHoldersAsync(coinType: CoinType, itemsCount: Int): Single<List<TokenHolder>> {
        if(!(coinType is CoinType.Erc20))
            return Single.error(Exception("Unsupported coinType: $coinType"))

        return horsysService.tokenHolders(coinType.address, itemsCount).map { response ->
            response.map {
                TokenHolder(
                    address = it.address,
                    share = it.share
                )
            }
        }
    }

    override fun getGlobalCoinMarketPointsAsync(currencyCode: String, timePeriod: TimePeriod): Single<List<GlobalCoinMarketPoint>> {
        if(!isTimePeriodSupported(timePeriod))
            return Single.error(Exception("Unsupported input parameter: $timePeriod"))

        return horsysService.globalCoinMarketPoints(timePeriod.title, currencyCode).map {
            val globalMarketPoints = it.map { response ->
                GlobalCoinMarketPoint(
                    timestamp = response.timestamp,
                    volume24h = response.volume24h,
                    marketCap = response.market_cap,
                    defiMarketCap = response.market_cap_defi,
                    btcDominance = response.dominance_btc,
                    defiTvl = response.tvl
                )
            }

            globalMarketPoints
        }
    }

    private fun isTimePeriodSupported(timePeriod: TimePeriod): Boolean{
        return when(timePeriod){
            TimePeriod.ALL -> false
            TimePeriod.DAY_START -> false
            TimePeriod.YEAR_1 -> false
            TimePeriod.DAY_200 -> false
            else -> true
        }
    }

    override fun getTopDefiTvlAsync(currencyCode: String, fetchDiffPeriod: TimePeriod, itemsCount: Int, chain: String?): Single<List<DefiTvl>> {

        val period = if(isTimePeriodSupported(fetchDiffPeriod)) fetchDiffPeriod.title else TimePeriod.HOUR_24.title

        return horsysService.defiTvl(currencyCode, period, chain).map { responseList ->
            val markets = responseList.mapNotNull { item ->
                item.code?.let { coinCode ->
                    item.coingecko_id?.let { coinGeckoId ->
                        if (item.coingecko_id.isNotEmpty()) {

                            val rateDiffPeriod = when (fetchDiffPeriod) {
                                TimePeriod.HOUR_1 -> item.tvl_diff_1h
                                TimePeriod.DAY_7 -> item.tvl_diff_7d
                                TimePeriod.DAY_14 -> item.tvl_diff_14d
                                TimePeriod.DAY_30 -> item.tvl_diff_30d
                                else -> item.tvl_diff_24h

                            } ?: BigDecimal.ZERO

                            getCoinType(coinGeckoId, InfoProvider.CoinGecko())?.let {
                                DefiTvl(
                                    CoinData(it, coinCode, item.name),
                                    item.tvl,
                                    rateDiffPeriod,
                                    item.tvl_rank ?: 0,
                                    item.chains
                                )
                            }
                        } else null
                    }
                }
            }
            markets.sortedByDescending { it.tvl }
        }
    }

    override fun getDefiTvlAsync(coinType: CoinType, currencyCode: String): Single<DefiTvl> {

        providerCoinsManager.getProviderIds(listOf(coinType), InfoProvider.CoinGecko()).firstOrNull()?.let { coinGeckoId ->
            return horsysService.coinDefiTvl(coinGeckoId, currencyCode).map { response ->
                response.code?.let{
                    DefiTvl(CoinData(coinType, it, response.name), response.tvl, BigDecimal.ZERO, response.tvl_rank?:0, response.chains)
                }
            }
        }?: return Single.error(Exception("No CoinGecko CoinId found for $coinType"))
    }

    override fun getDefiTvlPointsAsync(coinType: CoinType, currencyCode: String, timePeriod: TimePeriod): Single<List<DefiTvlPoint>> {

        if(!isTimePeriodSupported(timePeriod))
            return Single.error(Exception("Unsupported input parameter: $timePeriod"))

        providerCoinsManager.getProviderIds(listOf(coinType), InfoProvider.CoinGecko()).firstOrNull()?.let { coinGeckoId ->
            return horsysService.defiTvlPoints(coinGeckoId, timePeriod.title ,currencyCode).map { responseList ->
                responseList.map { responseItem ->
                    DefiTvlPoint(responseItem.timestamp, responseItem.tvl)
                }
            }
        }?: return Single.error(Exception("No CoinGecko CoinId found for $coinType"))
    }
}
