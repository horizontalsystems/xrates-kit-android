package io.horizontalsystems.xrateskit.demo.chartdemo

import io.horizontalsystems.xrateskit.demo.chartdemo.entities.CurrencyValue
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.NumberFormat
import java.util.*

class NumberFormatter {

    private val FIAT_BIG_NUMBER_EDGE = "1000".toBigDecimal()
    private val FIAT_TEN_CENT_EDGE = "0.1".toBigDecimal()

    private var formatters: MutableMap<String, NumberFormat> = mutableMapOf()

    fun formatForRates(currencyValue: CurrencyValue, trimmable: Boolean, maxFraction: Int?): String? {
        val value = currencyValue.value.abs()

        val customFormatter = getFormatter(Locale.ENGLISH) ?: return null

        when {
            maxFraction != null -> customFormatter.maximumFractionDigits = maxFraction
            value.compareTo(BigDecimal.ZERO) == 0 -> customFormatter.minimumFractionDigits = if (trimmable) 0 else 2
            value < FIAT_TEN_CENT_EDGE -> customFormatter.maximumFractionDigits = 4
            value >= FIAT_BIG_NUMBER_EDGE && trimmable -> customFormatter.maximumFractionDigits = 0
            else -> customFormatter.maximumFractionDigits = 2
        }

        val formatted = customFormatter.format(value)

        return "${currencyValue.currency.symbol}$formatted"
    }

    private fun getFormatter(locale: Locale): NumberFormat? {
        return formatters[locale.language] ?: run {
            val newFormatter = NumberFormat.getInstance(locale).apply {
                roundingMode = RoundingMode.HALF_EVEN
            }
            formatters[locale.language] = newFormatter
            return newFormatter
        }
    }
}
