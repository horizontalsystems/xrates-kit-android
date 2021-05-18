package io.horizontalsystems.xrateskit

import android.content.Context
import io.horizontalsystems.xrateskit.entities.ProviderCoinsResource
import java.io.InputStream
import java.net.URL

interface ProviderCoinsResourceProvider {
    fun getDataNewerThan(version: Int?): ProviderCoinsResource?
}

class ProviderCoinsResourceProviderImpl : ProviderCoinsResourceProvider {
    private val concreteProviders = mutableListOf<ProviderCoinsResourceProvider>()

    override fun getDataNewerThan(version: Int?): ProviderCoinsResource? {
        for (provider in concreteProviders) {
            provider.getDataNewerThan(version)?.let {
                return it
            }
        }

        return null
    }

    fun addProvider(provider: ProviderCoinsResourceProvider) {
        concreteProviders.add(provider)
    }
}

class LocalProviderCoinsResourceProvider(private val context: Context) : ProviderCoinsResourceProvider {
    private val providerCoinsFileName = "provider.coins.json"

    override fun getDataNewerThan(version: Int?): ProviderCoinsResource? {
        // if version is not null it means the local file has been already parsed before
        if (version != null) return null

        return ProviderCoinsResource.parseFile(true, context.assets.open(providerCoinsFileName))
    }
}

class RemoteGitHubProviderCoinsResourceProvider(private val path: String) : ProviderCoinsResourceProvider {

    override fun getDataNewerThan(version: Int?): ProviderCoinsResource? {
        val inputStream: InputStream

        try {
            inputStream = URL(path).openStream()
        } catch (e: Exception) {
            return null
        }

        val coinInfoResource = ProviderCoinsResource.parseFile(false, inputStream)
        return if (coinInfoResource.version != version) {
            coinInfoResource
        } else {
            null
        }
    }
}