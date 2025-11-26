package io.github.gustavlindberg99.weather

import org.json.JSONObject
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

data class DefconIntel(
    val level: Int,
    val description: String,
    val timestamp: Long
) {
    //Serializes the intel snapshot for persistence.
    fun toJson(): String {
        val json = JSONObject()
        json.put(KEY_LEVEL, level)
        json.put(KEY_DESCRIPTION, description)
        json.put(KEY_TIMESTAMP, timestamp)
        return json.toString()
    }

    companion object {
        private const val KEY_LEVEL = "defconLevel"
        private const val KEY_DESCRIPTION = "description"
        private const val KEY_TIMESTAMP = "timestamp"
        private const val API_TIMESTAMP_KEY = "timestamp"

        //Parses intel returned from the remote API.
        fun fromNetwork(json: JSONObject): DefconIntel {
            return DefconIntel(
                json.optInt(KEY_LEVEL, 5),
                json.optString(KEY_DESCRIPTION, ""),
                parseTimestamp(json.optString(API_TIMESTAMP_KEY))
            )
        }

        //Restores intel from cached JSON string.
        fun fromJson(raw: String?): DefconIntel? {
            if (raw.isNullOrBlank()) {
                return null
            }
            return try {
                val json = JSONObject(raw)
                DefconIntel(
                    json.optInt(KEY_LEVEL, 5),
                    json.optString(KEY_DESCRIPTION, ""),
                    json.optLong(KEY_TIMESTAMP, System.currentTimeMillis())
                )
            }
            catch (_: Exception) {
                null
            }
        }

        //Converts the ISO timestamp to epoch millis.
        private fun parseTimestamp(value: String?): Long {
            if (value.isNullOrBlank()) {
                return System.currentTimeMillis()
            }
            return try {
                val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                format.timeZone = TimeZone.getTimeZone("UTC")
                format.parse(value)?.time ?: System.currentTimeMillis()
            }
            catch (_: ParseException) {
                System.currentTimeMillis()
            }
        }
    }
}

