package io.horizontalsystems.xrateskit.api

import com.eclipsesource.json.Json
import com.eclipsesource.json.JsonObject
import com.eclipsesource.json.JsonValue
import java.io.OutputStream
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.logging.Logger
import javax.net.ssl.HttpsURLConnection

class ApiManager {
    private val logger = Logger.getLogger("ApiManager")

    fun getJson(uri: String, body: String): JsonObject {

        logger.info("Doing POST request: $uri")

        val conn = URL(uri).openConnection() as HttpsURLConnection
        conn.requestMethod = "POST"
        return conn
            .apply {
                connectTimeout = 18000
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
}
