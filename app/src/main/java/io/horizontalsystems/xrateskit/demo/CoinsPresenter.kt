package io.horizontalsystems.xrateskit.demo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.entities.ChartInfo
import io.horizontalsystems.xrateskit.entities.CoinData
import io.horizontalsystems.xrateskit.entities.MarketInfo
import java.util.concurrent.Executors

class CoinsPresenter(val view: CoinsView, private val interactor: CoinsInteractor) : ViewModel() {
    var currencies = listOf("USD", "EUR")
    var currency = App.baseCurrency

    private var items = listOf<CoinViewItem>()

    private val executor = Executors.newSingleThreadExecutor()
    private val enabledCoins: List<CoinViewItem>
        get() = items.filter { it.isChecked }

    fun onLoad(coinDatas: List<CoinData>) {
        executor.submit {
            items = coinDatas.map { CoinViewItem(it) }

            interactor.subscribeToMarketInfo(currency)

            //test fetching rates for top 100 coins
            //interactor.getTopList(100, currency)

            view.updateCoins(items)
        }
    }

    fun onSwitch(item: CoinViewItem, isChecked: Boolean) {
        executor.execute {
            item.isChecked = isChecked

            val coinTypes = enabledCoins.map { it.coinData.type }

            interactor.set(coinTypes)
            interactor.subscribeToChartInfo(coinTypes, currency)

            syncMarketInfo()
            syncChartInfo()

            view.updateCoins(items)
        }
    }

    fun onRefresh() {
        interactor.refresh()
    }

    fun onChangeCurrency(newCurrency: String) {
        executor.submit {
            currency = newCurrency

            interactor.set(currency)
            interactor.subscribeToMarketInfo(currency)

            syncMarketInfo()
            syncChartInfo()

            view.updateView()
        }
    }

    fun onUpdateMarketInfo(marketInfo: Map<CoinType, MarketInfo>) {
        executor.submit {
            items.forEach { item ->
                marketInfo[item.coinData.code]?.let {
                    item.marketInfo = it
                }
            }

            view.updateView()
        }
    }

    fun onUpdateChartInfo(chartInfo: ChartInfo, coinType: CoinType) {
        executor.submit {
            updateChartInfo(ChartInfoState.Loaded(chartInfo), coinType)
        }
    }

    fun onFailChartInfo(coinType: CoinType) {
        executor.submit {
            updateChartInfo(ChartInfoState.Failed, coinType)
        }
    }

    private fun syncMarketInfo() {
        enabledCoins.forEach { item ->
            item.marketInfo = interactor.marketInfo(item.coinData.type, currency)
        }
    }

    private fun syncChartInfo() {
        enabledCoins.forEach { item ->
            val chartInfo = interactor.chartInfo(item.coinData.type, currency)
            item.chartInfoState = chartInfo?.let { ChartInfoState.Loaded(it) } ?: ChartInfoState.Loading
        }
    }

    private fun updateChartInfo(chartInfoState: ChartInfoState, coinType: CoinType) {
        items.find { it.coinData.type == coinType }?.let { item ->
            item.chartInfoState = chartInfoState
        }

        view.updateView()
    }

    // -------------------------

    class Factory() : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val view = CoinsView()
            val interactor = CoinsInteractor(App.ratesManager)
            val presenter = CoinsPresenter(view, interactor)
            interactor.presenter = presenter

            return presenter as T
        }
    }
}

class CoinViewItem(
    val coinData: CoinData,
    var isChecked: Boolean = false,
    var marketInfo: MarketInfo? = null,
    var chartInfoState: ChartInfoState = ChartInfoState.Loading
)

sealed class ChartInfoState {
    object Loading : ChartInfoState()
    class Loaded(val chartInfo: ChartInfo) : ChartInfoState()
    object Failed : ChartInfoState()
}

