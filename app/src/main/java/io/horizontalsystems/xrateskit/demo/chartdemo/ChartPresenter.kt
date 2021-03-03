package io.horizontalsystems.xrateskit.demo.chartdemo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.chartview.ChartView
import io.horizontalsystems.chartview.models.ChartPoint
import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.demo.App
import io.horizontalsystems.xrateskit.demo.chartdemo.entities.Currency
import io.horizontalsystems.xrateskit.demo.chartdemo.entities.CurrencyValue
import io.horizontalsystems.xrateskit.entities.ChartInfo
import java.util.concurrent.Executors

class ChartPresenter(
        val view: ChartActivityView,
        val rateFormatter: ChartView.RateFormatter,
        private val currency: Currency,
        private val interactor: ChartInteractor)
    : ViewModel() {

    private val coinType = CoinType.Bitcoin

    private val executor = Executors.newSingleThreadExecutor()

    fun onLoad() {
        executor.submit {
            interactor.chartInfo(coinType, currency.code)?.let {
                view.updateChart(it)
            }

            interactor.subscribeToChartInfo(coinType, currency.code)
        }
    }

    fun onUpdateChartInfo(chartInfo: ChartInfo) {
        executor.submit {
            view.updateChart(chartInfo)
        }
    }

    fun onTouchSelect(point: ChartPoint) {
        val currencyValue = CurrencyValue(currency, point.value.toBigDecimal())
        view.showSelectedPoint(Triple(point.timestamp, currencyValue, ChartView.ChartType.DAILY))
    }

    // -------------------------

    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val currency = Currency(App.baseCurrency, "$")

            val view = ChartActivityView()
            val formatter = RateFormatter(currency)

            val interactor = ChartInteractor(App.ratesManager)
            val presenter = ChartPresenter(view, formatter, currency, interactor)

            interactor.presenter = presenter

            return presenter as T
        }
    }
}
