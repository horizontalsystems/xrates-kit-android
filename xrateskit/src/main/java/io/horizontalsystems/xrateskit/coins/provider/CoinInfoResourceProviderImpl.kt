package io.horizontalsystems.xrateskit.coins.provider

import io.horizontalsystems.xrateskit.entities.CoinInfoResource

class CoinInfoResourceProviderImpl : CoinInfoResourceProvider {

    private val concreteProviders = mutableListOf<CoinInfoResourceProvider>()

    override fun getDataNewerThan(version: Int?): CoinInfoResource? {
        for (provider in concreteProviders) {
            provider.getDataNewerThan(version)?.let {
                return it
            }
        }

        return null
    }

    fun addProvider(coinInfoResourceProvider: CoinInfoResourceProvider) {
        concreteProviders.add(coinInfoResourceProvider)
    }
}
