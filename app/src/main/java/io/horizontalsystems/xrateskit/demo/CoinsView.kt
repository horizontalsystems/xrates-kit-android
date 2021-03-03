package io.horizontalsystems.xrateskit.demo

import androidx.lifecycle.MutableLiveData

class CoinsView {
    val reload = MutableLiveData<Void?>()
    val updateCoins = MutableLiveData<List<CoinViewItem>>()

    fun updateView() {
        reload.postValue(null)
    }

    fun updateCoins(data: List<CoinViewItem>) {
        updateCoins.postValue(data)
    }
}
