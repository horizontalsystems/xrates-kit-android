package io.horizontalsystems.xrateskit.demo

import android.util.Log
import io.horizontalsystems.xrateskit.entities.ChartInfo
import io.horizontalsystems.xrateskit.entities.ChartType
import io.horizontalsystems.xrateskit.entities.Coin
import io.horizontalsystems.xrateskit.entities.MarketInfo
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class CoinsInteractor(private val ratesManager: RatesManager) {
    var presenter: CoinsPresenter? = null

    private var marketInfoDisposables = CompositeDisposable()
    private var chartInfoDisposables = CompositeDisposable()
    private var topListDisposable: Disposable? = null

    fun set(coins: List<Coin>) {
        ratesManager.set(coins)
    }

    fun set(currency: String) {
        ratesManager.set(currency)
    }

    fun refresh() {
        ratesManager.refresh()
    }

    fun subscribeToMarketInfo(currency: String) {
        marketInfoDisposables.clear()

        ratesManager.marketInfoObservable(currency)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe {
                    presenter?.onUpdateMarketInfo(it)
                }.let {
                    marketInfoDisposables.add(it)
                }
    }

    fun subscribeToChartInfo(coins: List<String>, currency: String) {
        chartInfoDisposables.clear()

        coins.forEach { coin ->
            ratesManager.chartInfoObservable(coin, currency, ChartType.DAILY)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe({
                        presenter?.onUpdateChartInfo(it, coin)
                    }, {
                        presenter?.onFailChartInfo(coin)
                    }).let {
                        chartInfoDisposables.add(it)
                    }
        }
    }

    fun getTopList(currency: String) {
        ratesManager.topList(currency)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                it.forEach { topMarket ->
                    Log.i("CoinsInteractor", "coinCode: ${topMarket.coinCode} coinName: ${topMarket.coinName} price: ${topMarket.marketInfo.rate} dailyDiff: ${topMarket.marketInfo.diff}")
                }
            }, {
                Log.e("CoinsInteractor", "exception", it)
            }).let {
                topListDisposable = it
            }
    }


    fun marketInfo(coin: String, currency: String): MarketInfo? {
        return ratesManager.marketInfo(coin, currency)
    }

    fun chartInfo(coin: String, currency: String): ChartInfo? {
        return ratesManager.chartInfo(coin, currency, ChartType.DAILY)
    }
}
