package io.horizontalsystems.xrateskit.demo.coinmarkets

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.demo.RatesManager
import io.horizontalsystems.xrateskit.entities.*
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class TopMarketsViewModel(val ratesManager: RatesManager) : ViewModel() {

    val searchCoinsLiveData = MutableLiveData<List<CoinData>>()
    val topMarkets = MutableLiveData<List<CoinMarket>>()
    val news = MutableLiveData<List<CryptoNews>>()
    val topDefiMarkets = MutableLiveData<List<DefiTvl>>()
    val coinMarketDetails = MutableLiveData<List<CoinMarketDetailsItem>>()
    val progressState = MutableLiveData<Boolean>()
    val tvlData = MutableLiveData<DefiTvl>()
    val globalMarketInfo = MutableLiveData<List<GlobalMarketInfoItem>>()

    private var disposables = CompositeDisposable()

    fun loadTopMarkets(timePeriod: TimePeriod) {
        progressState.postValue(true)
        ratesManager.topList(255, "USD", timePeriod)
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

    fun loadNews() {
        progressState.postValue(true)
        ratesManager.getNewsAsync()
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({ newsResponse ->
                news.postValue(newsResponse)
                progressState.postValue(false)
            }, {
                println("Error !!! ${it.message}")
                progressState.postValue(false)
            })
            .let {
                disposables.add(it)
            }
    }

    fun loadTopDefiMarkets() {
        progressState.postValue(true)
        ratesManager.topDefiTvlList(255, "USD")
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({ topDefiMarketList ->
                topDefiMarkets.postValue(topDefiMarketList)
                progressState.postValue(false)
            }, {
                println("Error !!! ${it.message}")
                progressState.postValue(false)
            })
            .let {
                disposables.add(it)
            }
    }

    fun loadMarketsByCategory(categoryId: String, timePeriod: TimePeriod) {
        progressState.postValue(true)
        ratesManager.getMarketsByCategory(categoryId, "USD", timePeriod)
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

    fun loadCoinInfo(coinType: CoinType = CoinType.Bitcoin) {
        progressState.postValue(true)
        ratesManager.getCoinMarketDetailsAsync(coinType, "USD", listOf("USD","ETH","BTC"), listOf(TimePeriod.HOUR_24, TimePeriod.DAY_30))
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                           coinMarketDetails.postValue(CoinMarketDetailsItem.getList(it))
                           progressState.postValue(false)
                       }, {
                           println("Error !!! ${it.message}")
                           progressState.postValue(false)
                       })
            .let {
                disposables.add(it)
            }
    }

    fun loadTvl(coinType: CoinType = CoinType.Bitcoin) {
        progressState.postValue(true)
        ratesManager.defiTvl(coinType, "USD")
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe({
                tvlData.postValue(it)
                progressState.postValue(false)
            }, {
                println("Error !!! ${it.message}")
                progressState.postValue(false)
            })
            .let {
                disposables.add(it)
            }
    }

    fun searchCoin(searchText: String) {
        searchCoinsLiveData.postValue(ratesManager.searchCoins(searchText))
    }

    fun loadFavorites(coinTypes: List<CoinType>, timePeriod: TimePeriod) {

        progressState.postValue(true)
        ratesManager.favorites(coinTypes, "USD", timePeriod)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({ favorites ->
                               topMarkets.postValue(favorites)
                               progressState.postValue(false)
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
