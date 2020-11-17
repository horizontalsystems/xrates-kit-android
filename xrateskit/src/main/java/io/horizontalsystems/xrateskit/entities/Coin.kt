package io.horizontalsystems.xrateskit.entities

data class Coin(
    val coinId: String,
    val code: String,
    val title: String = "",
    var type: CoinType = CoinType.Undefined)