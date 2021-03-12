package io.horizontalsystems.xrateskit.api

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.eclipsesource.json.JsonValue
import java.io.OutputStream
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.logging.Logger
import javax.net.ssl.HttpsURLConnection

class ApiManager(val rateLimit: Int) {
    private val logger = Logger.getLogger("ApiManager")

    private val delayMillis = rateLimit
    private var lastRequestTime = System.currentTimeMillis()

    fun getJson(uri: String, body: String): JsonObject {
        delayIfNeeded()

        logger.info("Doing POST request: $uri")

        val conn = URL(uri).openConnection() as HttpsURLConnection
        conn.requestMethod = "POST"
        return conn
            .apply {
                connectTimeout = 30000
                readTimeout = 60000

                setRequestProperty("Content-Type", "application/json");
                setRequestProperty("Accept", "application/json")
                doOutput = true
                doInput = true
                requestMethod = "POST"

                val os: OutputStream = outputStream
                os.write(body.toByteArray(StandardCharsets.UTF_8))
                os.flush()
                os.close()
            }
            .inputStream
            .use {
                Json.parse(it.bufferedReader()).asObject()
            }
    }

    @Throws
    fun getJson(uri: String, requestProperties: Map<String, String> = mapOf()): JsonObject {
        return getJsonValue(uri, requestProperties).asObject()
    }

    @Throws
    fun getJsonValue(uri: String, requestProperties: Map<String, String> = mapOf()): JsonValue {
        delayIfNeeded()

        logger.info("Fetching $uri")

        return URL(uri)
            .openConnection()
            .apply {
                connectTimeout = 5000
                readTimeout = 60000
                setRequestProperty("Accept", "application/json")
                requestProperties.forEach { (key, value) ->
                    setRequestProperty(key, value)
                }
            }
            .getInputStream()
            .use {
                Json.parse(it.bufferedReader())
            }
    }

    @Synchronized private fun delayIfNeeded() {
        val currentTimeMillis = System.currentTimeMillis()
        val timePassedFromLastRequest =  currentTimeMillis - lastRequestTime
        val timeToWait = delayMillis - timePassedFromLastRequest
        if (timeToWait > 0) {
            logger.info("Need to delay for $timeToWait millis")
            Thread.sleep(timeToWait)
        } else{
            logger.info("No Need to delay for ($timeToWait millis)")
        }
        lastRequestTime = currentTimeMillis
    }
}