package io.horizontalsystems.xrateskit.api

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.eclipsesource.json.JsonValue
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit
import java.util.logging.Logger

class ApiManager private constructor(private val requestDelay: Int, private val httpClient: OkHttpClient) {
    private val logger = Logger.getLogger("ApiManager")
    private var lastRequestTime = 0L

    @Throws
    fun getJson(uri: String): JsonObject = getJsonValue(uri).asObject()

    @Throws
    fun getJsonValue(uri: String): JsonValue {
        delayIfNeeded()

        logger.info("Fetching $uri")

        val request = Request.Builder()
                .addHeader("Accept", "application/json")
                .url(uri)
                .build()

        return Json.parse(httpClient.newCall(request).execute().body!!.charStream())
    }

    @Synchronized
    private fun delayIfNeeded() {
        if (requestDelay == 0) return

        val currentTime = System.currentTimeMillis()
        val timePassedFromLastRequest = currentTime - lastRequestTime
        val timeToWait = requestDelay - timePassedFromLastRequest
        if (timeToWait > 0) {
            logger.info("Need to delay for $timeToWait millis")
            Thread.sleep(timeToWait)
        }

        lastRequestTime = currentTime
    }

    companion object {
        private val httpClient = OkHttpClient.Builder()
                .connectTimeout(5000, TimeUnit.MILLISECONDS)
                .readTimeout(60000, TimeUnit.MILLISECONDS)
                .build()

        fun create(minDelayBetweenRequests: Int) = ApiManager(minDelayBetweenRequests, httpClient)
    }
}