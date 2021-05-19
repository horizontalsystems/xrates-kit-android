package io.horizontalsystems.xrateskit.coins

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.coins.provider.CoinExternalIdsSyncer
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.*
import io.horizontalsystems.xrateskit.providers.InfoProvider
import io.horizontalsystems.xrateskit.providers.coingecko.CoinGeckoProvider
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class ProviderCoinsManager(
    private val storage: IStorage,
    private val coinExternalIdsSyncer: CoinExternalIdsSyncer
) {

    private val disposable = CompositeDisposable()
    private val priorityUpdateInterval: Long = 10 * 24 * 60 * 60 // 10 days in seconds
    private val categorizedCoinPriority: Int = 0
    private val currentTimestamp: Long
        get() = Date().time / 1000

    var coinGeckoProvider: CoinGeckoProvider? = null

    private fun updatePriorities(topCoins: List<CoinMarket>) {
        val priorityCoins = mutableMapOf<CoinType, Int>()

        storage.getCategorizedCoinTypes().forEach { coinType ->
            priorityCoins[coinType] = categorizedCoinPriority
        }

        topCoins.forEachIndexed { index, coinMarket ->
            val coinType = coinMarket.data.type
            if (!priorityCoins.containsKey(coinType)) {
                priorityCoins[coinType] = index + 1
            }
        }
        storage.clearPriorities()

        priorityCoins.forEach { (coinType, priority) ->
            storage.setPriorityForCoin(coinType, priority)
        }

        storage.saveResourceInfo(
            ResourceInfo(
                ResourceType.PROVIDER_COINS_PRIORITY,
                "ProviderCoinsPriority",
                currentTimestamp
            )
        )
    }

    fun sync(): Single<Unit> {
        return Single.create { emitter ->
            coinExternalIdsSyncer.sync()
            emitter.onSuccess(Unit)
        }
    }

    fun updatePriorities() {
        val coinsPriorityUpdateTimestamp = storage.getResourceInfo(ResourceType.PROVIDER_COINS_PRIORITY)?.updatedAt ?: 0
        if (currentTimestamp - coinsPriorityUpdateTimestamp < priorityUpdateInterval) return

        coinGeckoProvider?.let { provider ->
            provider.getTopCoinMarketsAsync("USD", TimePeriod.HOUR_24, 1000)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe({ topCoins ->
                    updatePriorities(topCoins)
                }, {

                }).let { disposable.add(it) }
        }
    }

    private fun platformPriority(coinType: CoinType): Int {
        return when(coinType) {
            is CoinType.Erc20 -> 1
            is CoinType.Bep20 -> 2
            is CoinType.Bep2 -> 3
            is CoinType.Unsupported -> 4
            else -> 0
        }
    }

    fun getProviderIds(coinTypes: List<CoinType>, provider: InfoProvider): List<String?> {
        return storage.getProviderCoins(coinTypes).mapNotNull {
            if(provider is InfoProvider.CoinGecko)
                it.coingeckoId
            else
                it.cryptocompareId
        }
    }

    fun getProviderId(coinType: CoinType, provider: InfoProvider) : String? {
        return storage.getProviderCoin(coinType)?.let {
            if(provider is InfoProvider.CoinGecko)
                it.coingeckoId
            else
                it.cryptocompareId
        }
    }

    fun getCoinTypes(providerCoinId: String, provider: InfoProvider): List<CoinType> {
        return storage.getCoinTypesByProviderCoinId(providerCoinId, provider).sortedBy {
            platformPriority(it)
        }
    }

    fun searchCoins(searchText: String): List<CoinData> {
        if(searchText.length < 2)
            return emptyList()

        return storage.searchCoins(searchText).map {
            CoinData(it.coinType, it.code, it.name)
        }
    }
}

sealed class ProviderCoinError: Exception() {
    class NoMatchingExternalId : ProviderCoinError()
}
