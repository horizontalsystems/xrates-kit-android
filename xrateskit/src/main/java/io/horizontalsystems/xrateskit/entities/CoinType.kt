package io.horizontalsystems.xrateskit.entities


sealed class CoinType(val id: Int) {
    object Bitcoin : CoinType(id = 1)
    object Litecoin : CoinType(id = 2)
    object BitcoinCash : CoinType(id = 3)
    object Dash : CoinType(id = 4)
    object Ethereum : CoinType(id = 5)
    class  Erc20(val address: String) : CoinType(id = 6)
    object Binance : CoinType(id = 7)
    object Zcash : CoinType(id = 8)
    object Eos : CoinType(id = 9)

    companion object{
        fun getTypeById(id: Int?) : CoinType? {

            return when (id) {
                1 -> Bitcoin
                2 -> Litecoin
                3 -> BitcoinCash
                4 -> Dash
                5 -> Ethereum
                6 -> Erc20("")
                7 -> Binance
                8 -> Zcash
                9 -> Eos
                else -> null
            }
        }
    }
}