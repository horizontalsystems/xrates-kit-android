package io.horizontalsystems.xrateskit.demo

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.entities.ChartInfo
import io.horizontalsystems.xrateskit.entities.ChartType
import io.horizontalsystems.xrateskit.entities.MarketInfo
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class CoinsInteractor(private val ratesManager: RatesManager) {
    var presenter: CoinsPresenter? = null

    private var marketInfoDisposables = CompositeDisposable()
    private var chartInfoDisposables = CompositeDisposable()
    private var topListDisposable: Disposable? = null

    fun set(coinTypes: List<CoinType>) {
        ratesManager.set(coinTypes)
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

    fun subscribeToChartInfo(coinTypes: List<CoinType>, currency: String) {
        chartInfoDisposables.clear()

        coinTypes.forEach { type ->
            ratesManager.chartInfoObservable(type, currency, ChartType.DAILY)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe({
                        presenter?.onUpdateChartInfo(it, type)
                    }, {
                        presenter?.onFailChartInfo(type)
                    }).let {
                        chartInfoDisposables.add(it)
                    }
        }
    }

    fun marketInfo(coinType: CoinType, currency: String): MarketInfo? {
        return ratesManager.marketInfo(coinType, currency)
    }

    fun chartInfo(coinType: CoinType, currency: String): ChartInfo? {
        return ratesManager.chartInfo(coinType, currency, ChartType.DAILY)
    }
}
