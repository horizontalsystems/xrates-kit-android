package io.horizontalsystems.xrateskit.api

sealed class InfoProvider(val id: String, var baseUrl: String = "", var accessKey: String? = null) {

    class CryptoCompare : InfoProvider("cryptocompare", "https://min-api.cryptocompare.com")
    class CoinPaprika : InfoProvider("coinpaprika", "https://api.coinpaprika.com/v1")
    class CoinGecko : InfoProvider("coingecko", "https://api.coingecko.com/api/v3")
    class GraphNetwork : InfoProvider("graphnetwork", "https://api.thegraph.com/subgraphs/name")
    class HorSys : InfoProvider("horsys", "https://info.horizontalsystems.xyz/api/v1")

}

sealed class ProviderError: Exception() {
    class ApiRequestLimitExceeded : ProviderError()
    class NoDataForCoin : ProviderError()
    class UnknownTypeError : ProviderError()
}
