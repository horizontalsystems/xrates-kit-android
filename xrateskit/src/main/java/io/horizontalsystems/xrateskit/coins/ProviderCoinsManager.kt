package io.horizontalsystems.xrateskit.coins

import android.content.Context
import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.api.InfoProvider
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.CoinData
import io.horizontalsystems.xrateskit.entities.ProviderCoinsResource
import io.horizontalsystems.xrateskit.entities.ResourceInfo
import io.horizontalsystems.xrateskit.entities.ResourceType

class ProviderCoinsManager(
    private val context: Context,
    private val storage: IStorage
) {

    private val providerCoinsFileName = "provider.coins.json"

    init {
        updateCoinIds()
    }

    private fun updateCoinIds() {
        val coinsResponse = ProviderCoinsResource.parseFile(context, providerCoinsFileName)
        val resourceInfo = storage.getResourceInfo(ResourceType.PROVIDER_COINS)

        val update = resourceInfo?.let {
            coinsResponse.version != it.version
        } ?: true

        if (update) {
            storage.saveProviderCoins(coinsResponse.providerCoins)
            storage.saveResourceInfo(ResourceInfo(ResourceType.PROVIDER_COINS, coinsResponse.version))
        }
    }

    fun getProviderIds(coinTypes: List<CoinType>, provider: InfoProvider ): List<String?> {
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
