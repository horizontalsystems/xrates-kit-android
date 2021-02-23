package io.horizontalsystems.xrateskit.coinmarkets

import io.horizontalsystems.xrateskit.api.CoinGeckoProvider
import io.horizontalsystems.xrateskit.core.*
import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CoinGeckoManager(
    private val coinGeckoProvider: CoinGeckoProvider,
    private val storage: IStorage,
    private val factory: Factory
): IInfoManager, ICoinMarketManager {

    private fun getCoinIds(coinCodes: List<String>): Single<List<String>> {

        val coinInfosSingle: Single<List<ProviderCoinInfo>>

        if(storage.getProviderCoinsInfoCount(coinGeckoProvider.provider.id) != 0)
            coinInfosSingle = Single.just(storage.getProviderCoinInfoByCodes(coinGeckoProvider.provider.id, coinCodes.map { it.toUpperCase() }))
        else{
            coinInfosSingle = coinGeckoProvider.getProviderCoinInfoAsync()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map { providerCoins ->
                    storage.saveProviderCoinInfo(providerCoins)
                    storage.getProviderCoinInfoByCodes(coinGeckoProvider.provider.id, coinCodes.map { it.toUpperCase() })
                }
        }

        return coinInfosSingle.map {
            it.map { coinInfo -> coinInfo.providerCoinId }
        }

    }

    override fun getTopCoinMarketsAsync(currency: String, fetchDiffPeriod: TimePeriod, itemsCount: Int): Single<List<CoinMarket>> {
        return coinGeckoProvider
            .getTopCoinMarketsAsync(currency, fetchDiffPeriod, itemsCount)
            .map { topMarkets ->
                val marketEntityList = topMarkets.map { factory.createMarketInfoEntity(it.coin, it.marketInfo) }
                storage.saveMarketInfo(marketEntityList)
                topMarkets
            }
    }

    override fun getCoinMarketsAsync(coinCodes: List<String>, currencyCode: String, fetchDiffPeriod: TimePeriod): Single<List<CoinMarket>> {
        return getCoinIds(coinCodes).flatMap { coinIds ->
            coinGeckoProvider
                .getCoinMarketsAsync(coinIds, currencyCode, fetchDiffPeriod)
                .map { markets ->
                    val marketEntityList = markets.map { factory.createMarketInfoEntity(it.coin, it.marketInfo) }
                    storage.saveMarketInfo(marketEntityList)
                    markets
                }
        }
    }

    override fun getCoinMarketDetailsAsync(coinCode: String, currencyCode: String, rateDiffCoinCodes: List<String>, rateDiffPeriods: List<TimePeriod>): Single<CoinMarketDetails> {
        return getCoinIds(listOf(coinCode)).flatMap { coinIdList ->
                coinGeckoProvider.getCoinMarketDetailsAsync(coinIdList.get(0), currencyCode, rateDiffCoinCodes, rateDiffPeriods)
        }
    }

    override fun destroy() {
        coinGeckoProvider.destroy()
    }
}
