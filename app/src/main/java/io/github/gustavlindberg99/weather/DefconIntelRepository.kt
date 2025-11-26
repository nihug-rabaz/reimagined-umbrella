package io.github.gustavlindberg99.weather

import android.content.Context
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

class DefconIntelRepository(context: Context) {
    private val applicationContext = context.applicationContext
    private val preferences =
        applicationContext.getSharedPreferences(PREFERENCE_NAME, Context.MODE_PRIVATE)
    private val queue: RequestQueue by lazy { Volley.newRequestQueue(applicationContext) }

    //Loads the last known intel snapshot from disk.
    fun getCachedIntel(): DefconIntel? {
        return DefconIntel.fromJson(preferences.getString(KEY_INTEL, null))
    }

    //Fetches a fresh intel snapshot from the API.
    fun refreshIntel(onSuccess: (DefconIntel) -> Unit, onError: (Throwable) -> Unit) {
        val request = JsonObjectRequest(
            Request.Method.GET,
            API_URL,
            null,
            { response ->
                try {
                    val intel = DefconIntel.fromNetwork(response)
                    cacheIntel(intel)
                    onSuccess(intel)
                }
                catch (error: Exception) {
                    onError(error)
                }
            },
            { error -> onError(error) }
        )
        queue.add(request)
    }

    //Persists the intel snapshot for offline usage.
    private fun cacheIntel(intel: DefconIntel) {
        preferences.edit().putString(KEY_INTEL, intel.toJson()).apply()
    }

    companion object {
        private const val PREFERENCE_NAME = "defcon_widget"
        private const val KEY_INTEL = "intel"
        private const val API_URL = "https://v0-pizza-watch-api.vercel.app/api/pizza-intel"
    }
}

