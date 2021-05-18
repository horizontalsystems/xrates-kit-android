package io.horizontalsystems.xrateskit.coins.provider

interface DataProvider<T> {
    fun getDataNewerThan(version: Int?): T?
}

class DataProviderChain<T> : DataProvider<T> {
    private val concreteProviders = mutableListOf<DataProvider<T>>()

    override fun getDataNewerThan(version: Int?): T? {
        for (provider in concreteProviders) {
            provider.getDataNewerThan(version)?.let {
                return it
            }
        }

        return null
    }

    fun addProvider(provider: DataProvider<T>) {
        concreteProviders.add(provider)
    }
}
