package io.horizontalsystems.xrateskit.coins.provider

import io.horizontalsystems.xrateskit.entities.ResourceInfo

interface DataProvider<T> {
    fun getDataNewerThan(resourceInfo: ResourceInfo?): Data<T>?
}

data class Data<T>(val versionId: String, val value: T)

class DataProviderChain<T> : DataProvider<T> {
    private val concreteProviders = mutableListOf<DataProvider<T>>()

    override fun getDataNewerThan(resourceInfo: ResourceInfo?): Data<T>? {
        for (provider in concreteProviders) {
            provider.getDataNewerThan(resourceInfo)?.let {
                return it
            }
        }

        return null
    }

    fun addProvider(provider: DataProvider<T>) {
        concreteProviders.add(provider)
    }
}
