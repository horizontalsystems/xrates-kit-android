package io.horizontalsystems.xrateskit.demo.coinmarkets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.xrateskit.demo.App

object TopMarketsModule {

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TopMarketsViewModel(App.ratesManager) as T
        }
    }
}
