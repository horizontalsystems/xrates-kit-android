package io.horizontalsystems.xrateskit.coinmarkets

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.core.IDefiMarketsProvider
import io.horizontalsystems.xrateskit.core.IInfoManager
import io.horizontalsystems.xrateskit.core.ITokenInfoProvider
import io.horizontalsystems.xrateskit.entities.*
import io.horizontalsystems.xrateskit.providers.coingecko.CoinGeckoProvider
import io.reactivex.Single

class TokenInfoManager(
    private val tokenInfoProvider: ITokenInfoProvider
) : IInfoManager {

    fun getTopTokenHoldersAsync(coinType: CoinType, itemsCount: Int): Single<List<TokenHolder>> {
        return tokenInfoProvider.getTopTokenHoldersAsync(coinType, itemsCount)
    }

    override fun destroy() {}
}
