package io.horizontalsystems.xrateskit.providers.horsys

import io.horizontalsystems.xrateskit.core.IGlobalCoinMarketProvider
import io.horizontalsystems.xrateskit.entities.GlobalCoinMarketPoint
import io.horizontalsystems.xrateskit.entities.TimePeriod
import io.horizontalsystems.xrateskit.providers.InfoProvider
import io.horizontalsystems.xrateskit.utils.RetrofitUtils
import io.reactivex.Single

class HorsysProvider : IGlobalCoinMarketProvider {

    override val provider: InfoProvider = InfoProvider.HorSys()

    private val horsysService: HorsysService by lazy {
        RetrofitUtils.build(provider.baseUrl).create(HorsysService::class.java)
    }

    override fun initProvider() {}

    override fun destroy() {}

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
}
