package io.horizontalsystems.xrateskit.api

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.eclipsesource.json.JsonValue
import java.net.URL
import java.util.logging.Logger

class ApiManager {
    private val logger = Logger.getLogger("ApiManager")

    @Throws
    fun getJson(uri: String): JsonObject {
        return getJsonValue(uri).asObject()
    }

    private fun getJsonValue(uri: String): JsonValue {
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
}
