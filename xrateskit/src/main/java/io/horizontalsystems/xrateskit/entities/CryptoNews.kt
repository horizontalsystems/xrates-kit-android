package io.horizontalsystems.xrateskit.entities

data class CryptoNews(
    val id: Int,
    val source: String,
    val timestamp: Long,
    val imageUrl: String?,
    val title: String,
    val url: String,
    val body: String,
    val categories: List<String>
)
