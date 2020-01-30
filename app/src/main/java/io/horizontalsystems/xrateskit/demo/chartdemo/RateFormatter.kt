package io.horizontalsystems.xrateskit.demo.chartdemo

import io.horizontalsystems.chartview.ChartView
import io.horizontalsystems.xrateskit.demo.App
import io.horizontalsystems.xrateskit.demo.chartdemo.entities.Currency
import io.horizontalsystems.xrateskit.demo.chartdemo.entities.CurrencyValue
import java.math.BigDecimal

class RateFormatter(private val currency: Currency) : ChartView.RateFormatter {
    override fun format(value: BigDecimal, maxFraction: Int?): String? {
        val currencyValue = CurrencyValue(currency, value)

        return App.numberFormatter.formatForRates(currencyValue, maxFraction = maxFraction, trimmable = false)
    }
}
