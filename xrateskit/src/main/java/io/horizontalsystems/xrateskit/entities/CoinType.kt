package io.horizontalsystems.xrateskit.entities


sealed class CoinType {
    object Undefined : CoinType()
    object Bitcoin : CoinType()
    object Litecoin : CoinType()
    object BitcoinCash : CoinType()
    object Dash : CoinType()
    object Ethereum : CoinType()
    class  Erc20(val address: String) : CoinType()
    object Binance : CoinType()
    object Zcash : CoinType()
    object Eos : CoinType()
}