package io.horizontalsystems.xrateskit.demo.topmarkets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.horizontalsystems.xrateskit.demo.RatesManager
import io.horizontalsystems.xrateskit.entities.TopMarket
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import java.util.*

class TopMarketsViewModel(val ratesManager: RatesManager) : ViewModel() {

    val topMarkets = MutableLiveData<List<TopMarket>>()
    val globalMarketInfo = MutableLiveData<List<GlobalMarketInfoItem>>()

    private var disposables = CompositeDisposable()

    fun loadTopMarkets() {
        ratesManager.topList(20, "USD")
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({ topMarketList ->
                           topMarkets.postValue(topMarketList)
            }, {

            })
            .let {
                disposables.add(it)
            }
    }

    fun loadGlobalMarketInfo() {
        ratesManager.globalMarketInfo("USD")
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({ globalInfo ->
                           globalMarketInfo.postValue(GlobalMarketInfoItem.getList(globalInfo))
                       }, {

                       })
            .let {
                disposables.add(it)
            }
    }

}
