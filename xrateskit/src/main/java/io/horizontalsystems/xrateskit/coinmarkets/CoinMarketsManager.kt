package io.horizontalsystems.xrateskit.coinmarkets

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.providers.coingecko.CoinGeckoProvider
import io.horizontalsystems.xrateskit.core.*
import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Single

class CoinMarketsManager(private val coinGeckoProvider: CoinGeckoProvider): IInfoManager, ICoinMarketManager {

    override fun getTopCoinMarketsAsync(currency: String, fetchDiffPeriod: TimePeriod, itemsCount: Int): Single<List<CoinMarket>> {
        return coinGeckoProvider.getTopCoinMarketsAsync(currency, fetchDiffPeriod, itemsCount)
    }

    override fun getCoinMarketsAsync(coinTypes: List<CoinType>, currencyCode: String, fetchDiffPeriod: TimePeriod): Single<List<CoinMarket>> {
        return coinGeckoProvider.getCoinMarketsAsync(coinTypes, currencyCode, fetchDiffPeriod)
    }

    override fun getCoinMarketDetailsAsync(coinType: CoinType, currencyCode: String, rateDiffCoinCodes: List<String>, rateDiffPeriods: List<TimePeriod>): Single<CoinMarketDetails> {
        return coinGeckoProvider.getCoinMarketDetailsAsync(coinType, currencyCode, rateDiffCoinCodes, rateDiffPeriods)
    }

    override fun destroy() {
        coinGeckoProvider.destroy()
    }
}
