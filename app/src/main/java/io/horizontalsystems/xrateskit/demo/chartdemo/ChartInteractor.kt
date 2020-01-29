package io.horizontalsystems.xrateskit.demo.chartdemo

import android.util.Log
import io.horizontalsystems.xrateskit.demo.RatesManager
import io.horizontalsystems.xrateskit.entities.ChartInfo
import io.horizontalsystems.xrateskit.entities.ChartType
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class ChartInteractor(private val ratesManager: RatesManager) {
    var presenter: ChartPresenter? = null

    private var chartInfoDisposables = CompositeDisposable()

    fun subscribeToChartInfo(coin: String, currency: String) {
        chartInfoDisposables.clear()

        ratesManager.chartInfoObservable(coin, currency, ChartType.DAILY)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                presenter?.onUpdateChartInfo(it)
            }, {
                Log.e("ChartInteractor", "exception", it)
            }).let {
                chartInfoDisposables.add(it)
            }
    }

    fun chartInfo(coin: String, currency: String): ChartInfo? {
        return ratesManager.chartInfo(coin, currency, ChartType.DAILY)
    }
}
