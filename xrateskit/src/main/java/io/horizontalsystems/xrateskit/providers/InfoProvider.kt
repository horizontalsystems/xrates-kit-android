package io.horizontalsystems.xrateskit.providers

sealed class InfoProvider(val id: String, var baseUrl: String = "", val rateLimit: Int = 0, var accessKey: String? = null) {

    class CryptoCompare : InfoProvider("cryptocompare", "https://min-api.cryptocompare.com/")
    class CoinGecko : InfoProvider("coingecko", "https://api.coingecko.com/api/v3/", 600)
    class HorSys : InfoProvider("horsys", "https://markets.horizontalsystems.xyz/api/v1/", 150)
    class DefiYield : InfoProvider("defiyield", "https://api.safe.defiyield.app/", 150, "87e8671e-8267-427c-92c3-4627833445ae")

}

sealed class ProviderError: Exception() {
    class ApiRequestLimitExceeded : ProviderError()
    class NoDataForCoin : ProviderError()
    class UnknownTypeError : ProviderError()
}
