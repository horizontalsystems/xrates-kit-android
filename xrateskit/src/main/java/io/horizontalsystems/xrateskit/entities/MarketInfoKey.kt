package io.horizontalsystems.xrateskit.entities

import io.horizontalsystems.coinkit.models.CoinType

data class MarketInfoKey(
        val coinType: CoinType,
        val currency: String
)

