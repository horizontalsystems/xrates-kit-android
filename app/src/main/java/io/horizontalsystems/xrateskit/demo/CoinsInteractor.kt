package io.horizontalsystems.xrateskit.demo

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.entities.ChartInfo
import io.horizontalsystems.xrateskit.entities.ChartType
import io.horizontalsystems.xrateskit.entities.LatestRate
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CoinsInteractor(private val ratesManager: RatesManager) {
    var presenter: CoinsPresenter? = null

    private var latestRatesDisposables = CompositeDisposable()
    private var chartInfoDisposables = CompositeDisposable()

    fun refresh(currencyCode: String) {
        ratesManager.refresh(currencyCode)
    }

    fun subscribeToMarketInfo(coinTypes: List<CoinType>, currency: String) {
        latestRatesDisposables.clear()

        ratesManager.getLatestRateAsync(coinTypes, currency)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe {
                    presenter?.onUpdateLatestRate(it)
                }.let {
                    latestRatesDisposables.add(it)
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

    fun latestRate(coinType: CoinType, currency: String): LatestRate? {
        return ratesManager.latestRate(coinType, currency)
    }

    fun chartInfo(coinType: CoinType, currency: String): ChartInfo? {
        return ratesManager.chartInfo(coinType, currency, ChartType.DAILY)
    }
}
