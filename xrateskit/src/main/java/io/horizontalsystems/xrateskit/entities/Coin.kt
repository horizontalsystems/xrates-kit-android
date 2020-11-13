package io.horizontalsystems.xrateskit.entities

class Coin(
    val coinId: String,
    val code: String,
    val title: String = "",
    val address: String = "",
    val type: CoinType = CoinType.UNDEFINED){
}