package io.horizontalsystems.xrateskit.core

import io.horizontalsystems.xrateskit.api.CryptoCompareResponse
import io.horizontalsystems.xrateskit.storage.Rate
import java.util.*

class Factory {
    fun createRate(data: CryptoCompareResponse): Rate {
        return Rate(data.coin, data.currency, data.value, Date().time / 1000)
    }
}
