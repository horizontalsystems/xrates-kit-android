package io.horizontalsystems.xrateskit.demo.chartdemo

import io.horizontalsystems.chartview.Chart
import io.horizontalsystems.xrateskit.demo.App
import io.horizontalsystems.xrateskit.demo.chartdemo.entities.Currency
import io.horizontalsystems.xrateskit.demo.chartdemo.entities.CurrencyValue
import java.math.BigDecimal

class RateFormatter(private val currency: Currency) : Chart.RateFormatter {
    override fun format(value: BigDecimal): String? {
        val currencyValue = CurrencyValue(currency, value)

        return App.numberFormatter.formatForRates(currencyValue, maxFraction = null, trimmable = false)
    }
}
