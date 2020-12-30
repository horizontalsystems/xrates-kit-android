package io.horizontalsystems.xrateskit.demo.topmarkets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.horizontalsystems.xrateskit.demo.RatesManager
import io.horizontalsystems.xrateskit.entities.TimePeriod
import io.horizontalsystems.xrateskit.entities.TopMarket
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class TopMarketsViewModel(val ratesManager: RatesManager) : ViewModel() {

    val topMarkets = MutableLiveData<List<TopMarket>>()
    val progressState = MutableLiveData<Boolean>()
    val globalMarketInfo = MutableLiveData<List<GlobalMarketInfoItem>>()

    private var disposables = CompositeDisposable()

    fun loadTopMarkets(timePeriod: TimePeriod) {
        progressState.postValue(true)
        ratesManager.topList(200, "USD", timePeriod)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({ topMarketList ->
                           topMarkets.postValue(topMarketList)
                           progressState.postValue(false)
                       }, {
                            println("Error !!! ${it.message}")
                            progressState.postValue(false)
                       })
            .let {
                disposables.add(it)
            }
    }

    fun loadTopDefiMarkets(timePeriod: TimePeriod) {
        progressState.postValue(true)
        ratesManager.topDefiList(200, "USD", timePeriod)
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({ topMarketList ->
                           progressState.postValue(false)
                           topMarkets.postValue(topMarketList)
                       }, {
                            println("Error !!! ${it.message}")
                            progressState.postValue(false)
                       })
            .let {
                disposables.add(it)
            }
    }

    fun loadGlobalMarketInfo() {
        progressState.postValue(true)
        ratesManager.globalMarketInfo("USD")
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({ globalInfo ->
                           progressState.postValue(false)
                           globalMarketInfo.postValue(GlobalMarketInfoItem.getList(globalInfo))
                       }, {
                            progressState.postValue(false)
                            println("Error !!! ${it.message}")
                       })
            .let {
                disposables.add(it)
            }
    }

}