package io.horizontalsystems.xrateskit.entities

import java.math.BigDecimal

class DefiTvl(
    val data: CoinData,
    val tvl: BigDecimal,
    val tvlDiff: BigDecimal
)

class DefiTvlPoint(
    val timestamp: Long,
    val tvl: BigDecimal,
)
