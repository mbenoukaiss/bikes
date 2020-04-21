package io.github.mbenoukaiss.bikes

import android.os.Bundle
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
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
                for(city in contract.cities ?: emptyArray()) {
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
        val list = findViewById<LinearLayout>(R.id.city_list)

        search.doOnTextChanged { text, _, _, _ ->
            list.removeAllViews()

            for(city in cities.keys) {
                if(StringUtils.containsIgnoreCase(city, text)) {
                    val tv = TextView(this)
                    tv.setOnClickListener {
                        
                    }
                    tv.text = city

                    list.addView(tv)
                }
            }
        }
    }
}
