package io.horizontalsystems.xrateskit.entities

import java.math.BigDecimal

class ChartInfo(
        val points: List<ChartPoint>,
        val startTimestamp: Long,
        val endTimestamp: Long,
        val diff: BigDecimal?
)
