package io.horizontalsystems.xrateskit.api

sealed class InfoProvider(val id: Int, var baseUrl: String = "", var accessKey: String? = null) {

    class CryptoCompare : InfoProvider(1, "https://min-api.cryptocompare.com")
    class CoinPaprika : InfoProvider(3, "https://api.coinpaprika.com/v1")
    class CoinGecko : InfoProvider(4, "https://api.coingecko.com/api/v3")
    class GraphNetwork : InfoProvider(5, "https://api.thegraph.com/subgraphs/name")

}