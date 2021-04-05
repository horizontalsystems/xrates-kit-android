package io.horizontalsystems.xrateskit.rates

import io.horizontalsystems.coinkit.models.CoinType

interface ILatestRatesCoinTypeDataSource {
    fun getCoinTypes(currencyCode: String): List<CoinType>
}
