package io.horizontalsystems.xrateskit.toplist

import io.horizontalsystems.xrateskit.api.CryptoCompareProvider
import io.horizontalsystems.xrateskit.entities.PriceInfo
import io.reactivex.Single

class TopListManager(private val provider: CryptoCompareProvider) {

    fun getTopList(currency: String): Single<List<PriceInfo>> {
        return provider.getTopListCoins(currency)
            .flatMap { coinCodes ->
                provider.getPriceInfo(coinCodes, currency)
            }
    }

}
