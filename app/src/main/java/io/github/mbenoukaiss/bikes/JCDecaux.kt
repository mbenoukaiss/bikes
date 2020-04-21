package io.github.mbenoukaiss.bikes

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.android.volley.Request.Method
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.github.mbenoukaiss.bikes.models.Contract
import io.github.mbenoukaiss.bikes.models.Station

class JCDecaux(private val context: Context?, private val key: String) {

    companion object {
        const val BASE_URL = "https://api.jcdecaux.com/vls/v3"
    }

    private val queue: RequestQueue = Volley.newRequestQueue(context)
    private val gson: Gson = GsonBuilder().create()

    private fun <T : Any?> request(url: String, type: Class<T>, callback: (T) -> Unit) {
        val urlWithKey = url + if (url.contains("?")) {
            "&apiKey=$key"
        } else {
            "?apiKey=$key"
        }

        val stringRequest = StringRequest(Method.GET, BASE_URL + urlWithKey,
            Response.Listener { response ->
                callback.invoke(gson.fromJson(response, type))
            },
            Response.ErrorListener {
                Log.e("JCDecaux", "Call to endpoint failed: $it")
                Toast.makeText(context, "Failed to retrieve informations", Toast.LENGTH_LONG).show()

            })

        queue.add(stringRequest)
    }

    fun stations(callback: (Array<Station>) -> Unit) {
        request("/stations", Array<Station>::class.java, callback)
    }

    fun contracts(callback: (Array<Contract>) -> Unit) {
        request("/contracts", Array<Contract>::class.java, callback)
    }

    fun stationsForContract(contract: String?, callback: (Array<Station>) -> Unit) {
        request("/stations?contract=$contract", Array<Station>::class.java, callback)
    }

}