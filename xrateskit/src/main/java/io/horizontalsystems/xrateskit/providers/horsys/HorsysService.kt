package io.horizontalsystems.xrateskit.providers.horsys

import io.reactivex.Single
import retrofit2.http.GET
import java.math.BigDecimal

interface HorsysService {
    @GET("markets/global/defi")
    fun marketsGlobalDefi(): Single<GlobalDefi>
}

data class GlobalDefi(
    val marketCap: BigDecimal?,
    val marketCapDiff24h: BigDecimal?,
    val totalValueLocked: BigDecimal?,
    val totalValueLockedDiff24h: BigDecimal?,
)
