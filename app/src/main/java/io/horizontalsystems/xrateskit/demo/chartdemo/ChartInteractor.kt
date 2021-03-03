package io.horizontalsystems.xrateskit.demo.chartdemo

import android.util.Log
import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.demo.RatesManager
import io.horizontalsystems.xrateskit.entities.ChartInfo
import io.horizontalsystems.xrateskit.entities.ChartType
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class ChartInteractor(private val ratesManager: RatesManager) {
    var presenter: ChartPresenter? = null

    private var chartInfoDisposables = CompositeDisposable()

    fun subscribeToChartInfo(coinType: CoinType, currency: String) {
        chartInfoDisposables.clear()

        ratesManager.chartInfoObservable(coinType, currency, ChartType.DAILY)
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

    fun chartInfo(coinType: CoinType, currency: String): ChartInfo? {
        return ratesManager.chartInfo(coinType, currency, ChartType.DAILY)
    }
}
