package io.horizontalsystems.xrateskit.coins.provider

import io.horizontalsystems.xrateskit.entities.CoinInfoResource
import io.horizontalsystems.xrateskit.entities.ResourceInfo
import io.horizontalsystems.xrateskit.entities.ResourceType
import io.horizontalsystems.xrateskit.storage.Storage

class CoinInfoSyncer(
    private val dataProvider: DataProvider<CoinInfoResource>,
    private val storage: Storage
) {
    fun sync() {
        val resourceInfo = storage.getResourceInfo(ResourceType.COIN_INFO)

        dataProvider.getDataNewerThan(resourceInfo)?.let { data ->
            val coinsResponse = data.value

            storage.deleteAllSecurityParameters()
            storage.deleteAllTreasuryCompanies()
            storage.deleteAllCoinTreasuries()
            storage.deleteAllCoinCategories()
            storage.deleteAllCoinLinks()
            storage.deleteAllCoinsCategories()
            storage.deleteAllCoinFunds()
            storage.deleteAllCoinsFunds()
            storage.deleteAllCoinFundCategories()
            storage.deleteAllExchangeInfo()

            storage.saveCoinInfos(coinsResponse.coinInfos)
            storage.saveCoinCategories(coinsResponse.coinsCategories)
            storage.saveCoinCategory(coinsResponse.categories)
            storage.saveCoinFund(coinsResponse.funds)
            storage.saveCoinFunds(coinsResponse.coinFunds)
            storage.saveCoinFundCategory(coinsResponse.fundCategories)
            storage.saveCoinLinks(coinsResponse.links)
            storage.saveExchangeInfo(coinsResponse.exchangeInfos)
            storage.saveCoinTreasuries(coinsResponse.coinTreasuries)
            storage.saveTreasuryCompanies(coinsResponse.treasuryCompanies)
            storage.saveSecurityParameters(coinsResponse.securityParameters)

            storage.saveResourceInfo(ResourceInfo(ResourceType.COIN_INFO, data.versionId))
        }
    }
}
