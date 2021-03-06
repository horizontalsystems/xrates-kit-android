package io.horizontalsystems.xrateskit.providers

sealed class InfoProvider(val id: String, var baseUrl: String = "", val rateLimit: Int = 0, var accessKey: String? = null) {

    class CryptoCompare : InfoProvider("cryptocompare", "https://min-api.cryptocompare.com")
    class CoinPaprika : InfoProvider("coinpaprika", "https://api.coinpaprika.com/v1", 150)
    class CoinGecko : InfoProvider("coingecko", "https://api.coingecko.com/api/v3", 600)
    class HorSys : InfoProvider("horsys", "https://info.horizontalsystems.xyz/api/v1", 150)

}

sealed class ProviderError: Exception() {
    class ApiRequestLimitExceeded : ProviderError()
    class NoDataForCoin : ProviderError()
    class UnknownTypeError : ProviderError()
}
