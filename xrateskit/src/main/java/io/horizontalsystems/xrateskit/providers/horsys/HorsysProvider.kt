package io.horizontalsystems.xrateskit.providers.horsys

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.coins.ProviderCoinsManager
import io.horizontalsystems.xrateskit.core.IDefiMarketsProvider
import io.horizontalsystems.xrateskit.core.IGlobalCoinMarketProvider
import io.horizontalsystems.xrateskit.entities.*
import io.horizontalsystems.xrateskit.providers.InfoProvider
import io.horizontalsystems.xrateskit.utils.RetrofitUtils
import io.reactivex.Single
import java.util.*

class HorsysProvider(
    private val providerCoinsManager: ProviderCoinsManager
) : IGlobalCoinMarketProvider, IDefiMarketsProvider {

    override val provider: InfoProvider = InfoProvider.HorSys()

    private val horsysService: HorsysService by lazy {
        RetrofitUtils.build(provider.baseUrl).create(HorsysService::class.java)
    }

    override fun initProvider() {}

    override fun destroy() {}

    private fun getCoinType(providerCoinId: String, provider: InfoProvider): CoinType? {
        return providerCoinsManager.getCoinTypes(providerCoinId.toLowerCase(Locale.ENGLISH), provider).firstOrNull()
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

    override fun getTopDefiTvlAsync(currencyCode: String, fetchDiffPeriod: TimePeriod, itemsCount: Int): Single<List<DefiTvl>> {
        return horsysService.defiMarkets(currencyCode).map { responseList ->
            val markets = responseList.mapNotNull { item ->
                item.coingecko_id?.let {
                    if(item.coingecko_id.isNotEmpty()){
                        getCoinType(item.coingecko_id, InfoProvider.CoinGecko())?.let {
                            DefiTvl(CoinData(it, item.code, item.name), item.tvl, item.tvl_diff_24h)
                        }
                    } else null
                }
            }
            markets.sortedByDescending { it.tvl }
        }
    }
}
