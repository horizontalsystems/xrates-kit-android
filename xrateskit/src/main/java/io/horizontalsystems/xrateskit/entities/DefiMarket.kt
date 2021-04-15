package io.horizontalsystems.xrateskit.entities

import java.math.BigDecimal

class DefiMarket(
    val data: CoinData,
    val tvl: BigDecimal,
    val tvlDiff24h: BigDecimal,
)
