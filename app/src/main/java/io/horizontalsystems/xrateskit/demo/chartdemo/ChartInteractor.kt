package io.horizontalsystems.xrateskit.demo.chartdemo

import android.util.Log
import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.demo.RatesManager
import io.horizontalsystems.xrateskit.entities.ChartInfo
import io.horizontalsystems.xrateskit.entities.ChartType
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class ChartInteractor(private val ratesManager: RatesManager) {
    var presenter: ChartPresenter? = null
    var defaultChartType: ChartType? = ChartType.WEEKLY

    private var cInfoDisposable: Disposable? = null


    fun observeChartInfo(coinType: CoinType, currencyCode: String, chartType: ChartType) {
        cInfoDisposable?.dispose()
        cInfoDisposable = ratesManager.chartInfoObservable(coinType, currencyCode, chartType)
            .delay(600, TimeUnit.MILLISECONDS)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ chartInfo ->
                presenter?.onUpdate(chartInfo)
            }, {
                Log.e("ChartInteractor", "exception", it)
            })
    }

    fun getChartInfo(coinType: CoinType, currencyCode: String, chartType: ChartType): ChartInfo? {
        return ratesManager.chartInfo(coinType, currencyCode, chartType)
    }


    fun clear() {
        cInfoDisposable?.dispose()
    }

}
