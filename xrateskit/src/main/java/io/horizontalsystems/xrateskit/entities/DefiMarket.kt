package io.horizontalsystems.xrateskit.entities

import java.math.BigDecimal

class DefiTvl(
    val data: CoinData,
    val tvl: BigDecimal,
    val tvlDiff: BigDecimal,
    val tvlRank: Int = 0,
)

class DefiTvlPoint(
    val timestamp: Long,
    val tvl: BigDecimal,
)
