package io.github.mbenoukaiss.bikes

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import io.github.mbenoukaiss.bikes.models.Contract
import io.github.mbenoukaiss.bikes.models.Station
import org.apache.commons.lang3.StringUtils
import java.util.*
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity() {

    private val cities: HashMap<String, Vector<Station>> = HashMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val api = JCDecaux(this, "JCDECAUX_API_KEY")

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

        for (city in cities.keys) {
            if (text.isNullOrBlank() || StringUtils.containsIgnoreCase(city, text)) {
                val button = Button(this)
                button.setBackgroundColor(Color.TRANSPARENT)
                button.setTextColor(Color.WHITE)
                button.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
                button.text = city
                button.setPadding(4, 0, 4, 4)

                button.setOnClickListener {

                }

                list.addView(button)
            }
        }
    }
}
