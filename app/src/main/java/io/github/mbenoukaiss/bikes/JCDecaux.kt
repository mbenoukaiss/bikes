package io.github.mbenoukaiss.bikes

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat.getSystemService
import com.android.volley.*
import com.android.volley.Request.Method
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.github.mbenoukaiss.bikes.models.Contract
import io.github.mbenoukaiss.bikes.models.Station
import java.io.UnsupportedEncodingException


class JCDecaux(
    private val context: Context,
    private val key: String,
    private val error: () -> Unit
) {

    companion object {
        const val BASE_URL = "https://api.jcdecaux.com/vls/v3"
    }

    private val queue: RequestQueue = Volley.newRequestQueue(context)
    private val gson: Gson = GsonBuilder().create()

    private fun <T : Any?> request(url: String, type: Class<T>, callback: (T) -> Unit) {
        if(isNetworkAvailable()) {
            val urlWithKey = url + if (url.contains("?")) {
                "&apiKey=$key"
            } else {
                "?apiKey=$key"
            }

            val request = UTF8StringRequest(Method.GET, BASE_URL + urlWithKey,
                Response.Listener { response ->
                    callback.invoke(gson.fromJson(response, type))
                },
                Response.ErrorListener {
                    Log.e("JCDecaux", "Call to endpoint failed: $it")
                    error.invoke()
                })

            queue.add(request)
            queue.start()
        } else {
            Log.e("JCDecaux", "No internet connection found")
            error.invoke()
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false

        return when {
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            actNw.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH) -> true
            else -> false
        }
    }

    /**
     * Retrieves all the stations and calls the
     * `callback` closure if the request was successful.
     */
    fun stations(callback: (Array<Station>) -> Unit) {
        request("/stations", Array<Station>::class.java, callback)
    }

    /**
     * Retrieves all the contracts and calls the
     * `callback` closure if the request was successful.
     */
    fun contracts(callback: (Array<Contract>) -> Unit) {
        request("/contracts", Array<Contract>::class.java, callback)
    }

    /**
     * A volley StringRequest that works with UTF-8 strings.
     */
    class UTF8StringRequest(
        method: Int, url: String,
        private val mListener: Response.Listener<String>,
        errorListener: Response.ErrorListener
    ) : Request<String>(method, url, errorListener) {

        override fun deliverResponse(response: String) {
            mListener.onResponse(response)
        }

        override fun parseNetworkResponse(response: NetworkResponse): Response<String> {
            var parsed: String
            val encoding = charset(HttpHeaderParser.parseCharset(response.headers))

            return try {
                parsed = String(response.data, encoding)
                val bytes = parsed.toByteArray(encoding)
                parsed = String(bytes, charset("UTF-8"))

                Response.success(parsed, HttpHeaderParser.parseCacheHeaders(response))
            } catch (e: UnsupportedEncodingException) {
                Response.error(ParseError(e))
            }
        }
    }
}

