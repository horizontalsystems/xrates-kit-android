package io.horizontalsystems.xrateskit.coinmarkets

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.api.CoinGeckoProvider
import io.horizontalsystems.xrateskit.core.*
import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers

class CoinMarketsManager(
    private val coinGeckoProvider: CoinGeckoProvider,
    private val storage: IStorage,
    private val factory: Factory
): IInfoManager, ICoinMarketManager {

    override fun getTopCoinMarketsAsync(currency: String, fetchDiffPeriod: TimePeriod, itemsCount: Int): Single<List<CoinMarket>> {
        return coinGeckoProvider
            .getTopCoinMarketsAsync(currency, fetchDiffPeriod, itemsCount)
            .map { topMarkets ->
                val marketEntityList = topMarkets.map {
                    factory.createMarketInfoEntity(it.data.type, it.marketInfo)
                }
                storage.saveMarketInfo(marketEntityList)
                topMarkets
            }
    }

    override fun getCoinMarketsAsync(coinTypes: List<CoinType>, currencyCode: String, fetchDiffPeriod: TimePeriod): Single<List<CoinMarket>> {
        return coinGeckoProvider
                .getCoinMarketsAsync(coinTypes, currencyCode, fetchDiffPeriod)
                .map { markets ->
                    val marketEntityList = markets.map { factory.createMarketInfoEntity(it.data.type, it.marketInfo) }
                    storage.saveMarketInfo(marketEntityList)
                    markets
        }
    }

    override fun getCoinMarketDetailsAsync(coinType: CoinType, currencyCode: String, rateDiffCoinCodes: List<String>, rateDiffPeriods: List<TimePeriod>): Single<CoinMarketDetails> {
        return coinGeckoProvider.getCoinMarketDetailsAsync(coinType, currencyCode, rateDiffCoinCodes, rateDiffPeriods)
    }

    override fun destroy() {
        coinGeckoProvider.destroy()
    }
}
