package io.horizontalsystems.xrateskit.coins.provider

import android.content.Context
import io.horizontalsystems.xrateskit.entities.CoinInfoResource

class CoinInfoResourceProviderImpl(context: Context) : CoinInfoResourceProvider {

    private val concreteProviders: List<CoinInfoResourceProvider> =
        listOf(
            LocalCoinInfoResourceProvider(context),
            RemoteGitHubCoinInfoResourceProvider(),
        )

    override fun getDataNewerThan(version: Int?): CoinInfoResource? {
        for (provider in concreteProviders) {
            provider.getDataNewerThan(version)?.let {
                return it
            }
        }

        return null
    }
}
