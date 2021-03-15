package io.horizontalsystems.xrateskit.api

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.eclipsesource.json.JsonValue
import java.net.URL
import java.util.logging.Logger

class ApiManager(private val requestDelay: Int) {
    private val logger = Logger.getLogger("ApiManager")
    private var lastRequestTime = 0L

    @Throws
    fun getJson(uri: String): JsonObject = getJsonValue(uri).asObject()

    @Throws
    fun getJsonValue(uri: String): JsonValue {
        delayIfNeeded()

        logger.info("Fetching $uri")

        return URL(uri)
            .openConnection()
            .apply {
                connectTimeout = 5000
                readTimeout = 60000
                setRequestProperty("Accept", "application/json")
            }
            .getInputStream()
            .use {
                Json.parse(it.bufferedReader())
            }
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
}