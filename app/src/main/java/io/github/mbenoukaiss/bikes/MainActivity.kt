package io.github.mbenoukaiss.bikes

import OfflineCache
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import io.github.mbenoukaiss.bikes.models.Contract
import io.github.mbenoukaiss.bikes.models.Station
import org.apache.commons.lang3.StringUtils
import java.util.*
import kotlin.collections.HashMap

class MainActivity : Activity() {

    private var cities: HashMap<String, Vector<Station>> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions(arrayOf(WRITE_EXTERNAL_STORAGE), 1)

        val cache = OfflineCache(this)
        val api = JCDecaux(this, "JCDECAUX_API_KEY") {
            val cities = cache.read()

            val text = if(cities == null) {
                "Failed to retrieve stations data and no cached data were found. Make sure you are connected to the internet"
            } else {
                "Failed to retrieve stations, they were loaded from cache. Displayed information may be outdated"
            }

            Toast.makeText(this, text, Toast.LENGTH_LONG).show()

            if(cities != null) {
                this.cities = cities
                initialize()
            }
        }

        api.contracts {
            //variables to avoid making API calls in loops
            val cityContract: HashMap<String, Contract> = HashMap()
            val contractStations: HashMap<String, Vector<Station>> = HashMap()

            for (contract in it) {
                for (city in contract.cities ?: emptyArray()) {
                    cityContract[city] = contract
                }
            }

            api.stations {
                for (station in it) {
                    val stations = contractStations.getOrPut(station.contractName) {
                        Vector<Station>()
                    }

                    stations.addElement(station)
                }

                for ((city, contract) in cityContract.entries) {
                    cities[city] = contractStations.getOrDefault(contract.name, Vector())
                }

                cache.asyncWrite(cities)
                initialize()
            }
        }
    }

    private fun initialize() {
        val search = findViewById<EditText>(R.id.search)

        search.doOnTextChanged { text, _, _, _ ->
            searchChanged(text.toString())
        }

        //show all cities when the app starts
        searchChanged(null)
    }

    private fun searchChanged(text: String?) {
        val list = findViewById<LinearLayout>(R.id.city_list)
        list.removeAllViews()

        for ((city, stations) in cities.entries) {
            if (text.isNullOrBlank() || StringUtils.containsIgnoreCase(city, text)) {
                val button = Button(this)
                button.setBackgroundColor(Color.TRANSPARENT)
                button.setTextColor(Color.WHITE)
                button.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                button.text = city

                button.setOnClickListener {
                    val intent = Intent(this, StationsActivity::class.java)
                    intent.putExtra("CITY_NAME", city)
                    intent.putExtra("STATIONS", stations)

                    startActivity(intent)
                }

                list.addView(button)
            }
        }
    }

}
