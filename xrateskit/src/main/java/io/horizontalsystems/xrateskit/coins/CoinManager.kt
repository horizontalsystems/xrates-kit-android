package io.horizontalsystems.xrateskit.coins

import io.horizontalsystems.xrateskit.core.IStorage
import io.horizontalsystems.xrateskit.entities.Coin
import io.horizontalsystems.xrateskit.entities.CoinEntity
import io.horizontalsystems.xrateskit.entities.CoinType

class CoinManager(private val storage: IStorage) {

    fun saveCoin(coins: List<Coin>){
        val entity = coins
                .map { coin ->
                    val address = if(coin.type is CoinType.Erc20)  (coin.type as CoinType.Erc20).address else ""
                    CoinEntity(coin.code, coin.title, coin.type?.id, address)
                }
        storage.saveCoins(entity)
    }

    fun identifyCoins(coins: List<Coin>){
        val coinCodes = coins.map { it.code }
        val savedCoins = storage.getCoinsByCodes(coinCodes)

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
}