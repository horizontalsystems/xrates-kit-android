package io.horizontalsystems.xrateskit.coins

import android.content.Context
import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.*
import io.horizontalsystems.xrateskit.providers.CoinGeckoProvider
import io.horizontalsystems.xrateskit.providers.InfoProvider
import io.reactivex.Single
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class ProviderCoinsManager(
    private val context: Context,
    private val storage: IStorage
) {

    private val providerCoinsFileName = "provider.coins.json"

    private val disposable = CompositeDisposable()
    private val priorityUpdateInterval: Long = 10 * 24 * 60 * 60 // 10 days in seconds
    private val categorizedCoinPriority: Int = 0
    private val currentTimestamp: Int
        get() = (Date().time / 1000).toInt()

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
                currentTimestamp
            )
        )
    }

    private fun updateCoinIds() {
        var coinsResponse = ProviderCoinsResource.parseFile(true, context, providerCoinsFileName)
        val resourceInfo = storage.getResourceInfo(ResourceType.PROVIDER_COINS)

        val update = resourceInfo?.let {
            coinsResponse.version != it.version
        } ?: true

        if (update) {
            coinsResponse = ProviderCoinsResource.parseFile(false, context, providerCoinsFileName)
            storage.saveProviderCoins(coinsResponse.providerCoins)
            storage.saveResourceInfo(ResourceInfo(ResourceType.PROVIDER_COINS, coinsResponse.version))
        }
    }

    fun sync(): Single<Unit> {
        return Single.create { emitter ->
            updateCoinIds()
            emitter.onSuccess(Unit)
        }
    }

    fun updatePriorities() {
        val coinsPriorityUpdateTimestamp = storage.getResourceInfo(ResourceType.PROVIDER_COINS_PRIORITY)?.version ?: 0
        if (currentTimestamp - coinsPriorityUpdateTimestamp < priorityUpdateInterval) return

        coinGeckoProvider?.let { provider ->
            provider.getTopCoinMarketsAsync("USD", TimePeriod.HOUR_24, 400)
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe({ topCoins ->
                    updatePriorities(topCoins)
                }, {

                }).let { disposable.add(it) }
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

    fun getCoinTypes(providerCoinId: String, provider: InfoProvider): List<CoinType> {
        return storage.getCoinTypesByProviderCoinId(providerCoinId, provider)
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
