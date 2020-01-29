package io.horizontalsystems.xrateskit.demo.chartdemo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.xrateskit.demo.App
import io.horizontalsystems.xrateskit.demo.chartdemo.chartview.ChartView
import io.horizontalsystems.xrateskit.demo.chartdemo.chartview.models.ChartPointFloat
import io.horizontalsystems.xrateskit.demo.chartdemo.entities.Currency
import io.horizontalsystems.xrateskit.demo.chartdemo.entities.CurrencyValue
import io.horizontalsystems.xrateskit.entities.ChartInfo
import java.util.concurrent.Executors

class ChartPresenter(val view: ChartActivityView, private val interactor: ChartInteractor) : ViewModel() {
    private val coinCode = "BTC"
    var currency = App.baseCurrency

    private val executor = Executors.newSingleThreadExecutor()

    fun onLoad() {
        executor.submit {
            interactor.chartInfo(coinCode, currency)?.let {
                view.updateChart(it)
            }
            interactor.subscribeToChartInfo(coinCode, currency)
        }
    }

    fun onUpdateChartInfo(chartInfo: ChartInfo) {
        executor.submit {
            view.updateChart(chartInfo)
        }
    }

    fun onTouchSelect(point: ChartPointFloat){
        val currencyValue = CurrencyValue(Currency(currency, "$"), point.value.toBigDecimal())
        view.showSelectedPoint(Triple(point.timestamp, currencyValue, ChartView.ChartType.DAILY))
    }

    // -------------------------

    class Factory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val view = ChartActivityView()
            val interactor = ChartInteractor(App.ratesManager)
            val presenter = ChartPresenter(view, interactor)
            interactor.presenter = presenter

            return presenter as T
        }
    }
}
