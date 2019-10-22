package io.horizontalsystems.xrateskit.entities

data class ChartPointKey(
        val coin: String,
        val currency: String,
        val chartType: ChartType
)

