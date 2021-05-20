package io.horizontalsystems.xrateskit.demo.chartdemo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.chartview.Chart
import io.horizontalsystems.chartview.models.PointInfo
import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.demo.App
import io.horizontalsystems.xrateskit.demo.chartdemo.entities.Currency
import io.horizontalsystems.xrateskit.demo.chartdemo.entities.CurrencyValue
import io.horizontalsystems.xrateskit.entities.ChartInfo
import io.horizontalsystems.xrateskit.entities.ChartType
import java.math.BigDecimal
import java.util.concurrent.Executors

class ChartPresenter(
    val view: ChartActivityView,
    val rateFormatter: Chart.RateFormatter,
    private val currency: Currency,
    private val interactor: ChartInteractor,
    private val factory: ChartViewFactory)
    : ViewModel() {

    private val coinType = CoinType.fromString("erc20|0x761d38e5ddf6ccf6cf7c55759d5210750b5d60f3")
    private val executor = Executors.newSingleThreadExecutor()
    private var chartType = ChartType.WEEKLY
    private var emaIsEnabled = false
    private var macdIsEnabled = false
    private var rsiIsEnabled = false

    fun onLoad() {
        executor.submit {
            view.setChartType(chartType)
            fetchChartInfo()
            fetchHistoInfo()
        }
    }

    private var chartInfo: ChartInfo? = null
        set(value) {
            field = value
            updateChartInfo()
        }

    fun onTouchSelect(point: PointInfo) {
        val price = CurrencyValue(currency, point.value.toBigDecimal())

        if (macdIsEnabled){
            view.showSelectedPointInfo(ChartPointViewItem(point.timestamp, price, null, point.macdInfo))
        } else {
            val volume = point.volume?.let { volume ->
                CurrencyValue(currency, volume.toBigDecimal())
            }
            view.showSelectedPointInfo(ChartPointViewItem(point.timestamp, price, volume, null))
        }
    }

    private fun updateChartInfo() {
        val info = chartInfo ?: return

        view.hideSpinner()

        try {
            view.showChartInfo(factory.createChartInfo(chartType, info, null))
        } catch (e: Exception) {
        }
    }

    fun updateHistoInfo(coinType: CoinType, currencyCode: String, timestamp: Long, rate: BigDecimal) {
        try {
            view.showHistoInfo(HistoInfoViewItem(coinType, timestamp, currencyCode, rate))
        } catch (e: Exception) {
        }
    }

    fun onSelect(type: ChartType) {
        if (chartType == type)
            return

        chartType = type
        interactor.defaultChartType = type

        fetchChartInfo()
    }

    fun toggleEma() {
        emaIsEnabled = !emaIsEnabled
        view.setEmaEnabled(emaIsEnabled)
    }

    fun toggleMacd() {
        if (rsiIsEnabled){
            toggleRsi()
        }

        macdIsEnabled = !macdIsEnabled
        view.setMacdEnabled(macdIsEnabled)
    }

    fun toggleRsi() {
        if (macdIsEnabled){
            toggleMacd()
        }

        rsiIsEnabled = !rsiIsEnabled
        view.setRsiEnabled(rsiIsEnabled)
    }

    private fun fetchHistoInfo() {
        interactor.observeHistoRate(coinType, currency.code, (System.currentTimeMillis()/1000) - 86400)
    }

    private fun fetchChartInfo() {
        view.showSpinner()

        chartInfo = interactor.getChartInfo(coinType, currency.code, chartType)
        interactor.observeChartInfo(coinType, currency.code, chartType)
    }

    fun onUpdate(chartInfo: ChartInfo) {
        this.chartInfo = chartInfo
    }

    // -------------------------

    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val currency = Currency(App.baseCurrency, "$")

            val view = ChartActivityView()
            val formatter = RateFormatter(currency)

            val interactor = ChartInteractor(App.ratesManager)
            val presenter = ChartPresenter(view, formatter, currency, interactor, ChartViewFactory())

            interactor.presenter = presenter

            return presenter as T
        }
    }
}
