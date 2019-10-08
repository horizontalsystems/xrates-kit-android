package io.horizontalsystems.xrateskit.storage

import io.horizontalsystems.xrateskit.core.IStorage

class Storage(database: Database) : IStorage {
    private val latestRateDao = database.latestRateDao
    private val historicalRateDao = database.historicalRateDao

    // LatestRate

    override fun saveLatestRate(rate: LatestRate) {
        latestRateDao.insert(rate)
    }

    override fun getLatestRate(coin: String, currency: String): LatestRate? {
        return latestRateDao.getRate(coin, currency)
    }

    // HistoricalRate

    override fun saveHistoricalRate(rate: HistoricalRate) {
        historicalRateDao.insert(rate)
    }

    override fun getHistoricalRate(coin: String, currency: String, timestamp: Long): HistoricalRate? {
        return historicalRateDao.getRate(coin, currency, timestamp)
    }
}
