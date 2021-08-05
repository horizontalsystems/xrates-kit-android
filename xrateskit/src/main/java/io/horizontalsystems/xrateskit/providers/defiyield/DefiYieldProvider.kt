package io.horizontalsystems.xrateskit.providers.defiyield

import io.horizontalsystems.coinkit.models.CoinType
import io.horizontalsystems.xrateskit.core.IAuditInfoProvider
import io.horizontalsystems.xrateskit.entities.AuditReport
import io.horizontalsystems.xrateskit.entities.Auditor
import io.horizontalsystems.xrateskit.providers.InfoProvider
import io.horizontalsystems.xrateskit.utils.RetrofitUtils
import io.reactivex.Single
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Logger

class DefiYieldProvider(private val defiyieldApiKey: String) : IAuditInfoProvider {

    private val logger = Logger.getLogger("DefiYieldProvider")
    override val provider: InfoProvider = InfoProvider.DefiYield()
    private val AUTH_BEARER_TOKEN = "Bearer ${defiyieldApiKey}"

    private val FILES_BASE_URL = "https://files.safe.defiyield.app/"
    private val defiYieldService: DefiYieldService by lazy {
        RetrofitUtils.build(provider.baseUrl).create(DefiYieldService::class.java)
    }

    override fun initProvider() {}
    override fun destroy() {}

    override fun getAuditReportsAsync(coinType: CoinType): Single<List<Auditor>> {

        val address = when(coinType){
            is CoinType.Bep20 -> coinType.address
            is CoinType.Erc20 -> coinType.address
            else -> null
        }

        if (address.isNullOrEmpty()) {
            return Single.error(Exception("Unsupported coinType: $coinType"))
        }

        return defiYieldService.auditInfo(AUTH_BEARER_TOKEN, InputParams.BodyTokensAddress(listOf(address))).map {
            val auditors = mutableListOf<Auditor>()

            it.firstOrNull()?.let { response ->
                logger.info("Audit info found for coin:${coinType} - Response Count:${response.partnerAudits.size}")

                response.partnerAudits.forEach { audit ->

                    audit.partner?.let { partner ->
                        var auditor = auditors.find { it.name.contentEquals(partner.name) }

                        if (auditor == null) {
                            auditor = Auditor(
                                id = partner.name.trim().toLowerCase().replace("\\s".toRegex(), "-"),
                                name = partner.name
                            )
                            auditors.add(auditor)
                        }

                        val auditReport = AuditReport(
                            name = audit.name,
                            issues = audit.tech_issues ?: 0,
                            timestamp = SimpleDateFormat("yyyy-MM-dd").parse(audit.date).time / 1000,
                            link = audit.audit_link?.let { "${FILES_BASE_URL}${it}" }
                        )

                        auditor.reports.add(auditReport)
                    }
                }
            }

            auditors
        }
    }
}