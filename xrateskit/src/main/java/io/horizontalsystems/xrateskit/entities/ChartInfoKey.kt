package io.horizontalsystems.xrateskit.entities

import io.horizontalsystems.coinkit.models.CoinType

data class ChartInfoKey(
        val coinType: CoinType,
        val currency: String,
        val chartType: ChartType
)

