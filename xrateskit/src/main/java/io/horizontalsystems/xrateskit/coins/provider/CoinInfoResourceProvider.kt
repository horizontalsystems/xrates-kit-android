package io.horizontalsystems.xrateskit.coins.provider

import io.horizontalsystems.xrateskit.entities.CoinInfoResource

interface CoinInfoResourceProvider {
    fun getDataNewerThan(version: Int?): CoinInfoResource?
}
