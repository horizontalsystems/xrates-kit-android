package io.horizontalsystems.xrateskit.providers.defiyield

import io.horizontalsystems.xrateskit.providers.defiyield.Response.AuditInfo
import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.POST

interface DefiYieldService {

    @Headers("Content-Type: application/json", "Accept: application/json")
    @POST("audit/address")
    fun auditInfo(
        @Header("Authorization") auth: String,
        @Body body: InputParams.BodyTokensAddress
    ): Single<List<AuditInfo>>

}

object InputParams {
    data class BodyTokensAddress(val addresses: List<String>)
}

object Response {
    data class AuditInfo(
        val partnerAudits: List<PartnerAudits>
    )

    data class PartnerAudits(
        val name: String,
        val date: String,
        val tech_issues: Int?,
        val audit_link: String?,
        val partner : Partner?
    )

    data class Partner(
        val name: String
    )
}
