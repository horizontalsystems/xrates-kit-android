package io.horizontalsystems.xrateskit.toplist

import io.horizontalsystems.xrateskit.api.CryptoCompareProvider
import io.horizontalsystems.xrateskit.entities.PriceInfo
import io.reactivex.Single

class TopListManager(private val provider: CryptoCompareProvider) {

    private var topCoinsCodes = mutableListOf<String>()
    private val maxSize = 100
    private val halfSize = 50

    fun getTopList(currency: String, shownItemSize: Int = 0): Single<List<PriceInfo>> {
        if (shownItemSize >= maxSize) return Single.just(listOf())

        return getCoinCodes(currency, shownItemSize)
            .flatMap { coinCodes ->
                provider.getPrices(coinCodes, currency)
            }
    }

    private fun getCoinCodes(currency: String, shownItemSize: Int): Single<List<String>> {
        return if (topCoinsCodes.isEmpty()) {
            provider.getTopListCoins(currency).doOnSuccess {
                topCoinsCodes.addAll(it)
            }.flatMap {
                Single.just(getPaged(it, shownItemSize))
            }
        } else {
            Single.just(getPaged(topCoinsCodes, shownItemSize))
        }
    }

    private fun getPaged(list: List<String>, currentlyShownSize: Int) =
        if (currentlyShownSize == 0) list.take(halfSize) else list.takeLast(halfSize)
}
