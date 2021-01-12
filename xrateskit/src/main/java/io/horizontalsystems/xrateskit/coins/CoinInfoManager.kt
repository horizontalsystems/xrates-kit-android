package io.horizontalsystems.xrateskit.coins

import io.horizontalsystems.xrateskit.core.ICoinInfoProvider
import io.horizontalsystems.xrateskit.core.IInfoManager
import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.Coin
import io.horizontalsystems.xrateskit.entities.CoinInfoEntity
import io.horizontalsystems.xrateskit.entities.CoinType
import io.horizontalsystems.xrateskit.entities.ProviderCoinInfo
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CoinInfoManager (
    private val coinInfoProvider: ICoinInfoProvider,
    private val storage: IStorage):IInfoManager {

    private var managerDisposables = CompositeDisposable()

    // Collect and Save coin info about Erc20 tokens
    init {
        if(!isCoinInfoExists()){
            coinInfoProvider.getCoinInfoAsync(CoinType.Ethereum)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe({ coinInfos ->
                               saveCoinInfo(coinInfos)
                           }, {
                           })
                .let {
                    managerDisposables.add(it)
                }
        }
    }

    fun saveProviderCoinInfo(providerCoinInfos: List<ProviderCoinInfo>){
        storage.saveProviderCoinInfo(providerCoinInfos)
    }

    fun getProviderCoinInfo(providerId:Int, coinCodes: List<String>): List<ProviderCoinInfo> {
        return storage.getProviderCoinInfoByCodes(providerId, coinCodes)
    }

    fun isProviderCoinInfoExists( providerId: Int): Boolean{
        return (storage.getProviderCoinsInfoCount(providerId) > 0)
    }

    fun isCoinInfoExists(): Boolean {
        return (storage.getCoinInfoCount() > 0)
    }

    fun saveCoinInfo(coins: List<Coin>){
        val entity = coins
                .map { coin ->
                    val address = if(coin.type is CoinType.Erc20)  (coin.type as CoinType.Erc20).address else ""
                    CoinInfoEntity(coin.code, coin.title, coin.type?.id, address)
                }
        storage.saveCoinInfo(entity)
    }

    fun identifyCoins(coins: List<Coin>){
        val coinCodes = coins.map { it.code }
        val savedCoins = storage.getCoinInfoByCodes(coinCodes)

        savedCoins.forEach {
            savedCoin ->
            coins.find { it.code.contentEquals(savedCoin.code) }?.let {
                coin ->
                val type = CoinType.getTypeById(savedCoin.type)
                if(type is CoinType.Erc20){
                    coin.type = CoinType.Erc20(savedCoin.contractAddress)
                } else {
                    coin.type = type
                }
            }
        }
    }

    override fun destroy() {
        managerDisposables.clear()
    }
}