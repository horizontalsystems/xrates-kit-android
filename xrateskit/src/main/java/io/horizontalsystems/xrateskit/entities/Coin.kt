package io.horizontalsystems.xrateskit.entities

data class Coin(
    val code: String,
    val title: String = "",
    var type: CoinType? = null)