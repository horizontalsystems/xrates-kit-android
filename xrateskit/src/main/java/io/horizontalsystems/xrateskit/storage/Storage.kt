package io.horizontalsystems.xrateskit.storage

import io.horizontalsystems.xrateskit.core.IStorage

class Storage(database: Database) : IStorage {
    private val rateDao = database.rateDao

    override fun saveRate(rate: Rate) {
        rateDao.insert(rate)
    }

    override fun getRate(coin: String, currency: String): Rate? {
        return rateDao.getRate(coin, currency)
    }
}
