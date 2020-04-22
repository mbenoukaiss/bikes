package io.github.mbenoukaiss.bikes

import android.R.attr.name
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.appcompat.app.AppCompatActivity
import io.github.mbenoukaiss.bikes.models.Station
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


class StationsActivity : AppCompatActivity() {

    private var map: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stations)

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        val map = findViewById<MapView>(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.minZoomLevel = 5.5

        val stations = intent.getSerializableExtra("STATIONS") as ArrayList<Station>
        val points = ArrayList<GeoPoint>()

        for (station in stations) {
            val position = GeoPoint(
                station.position.latitude.toDouble(),
                station.position.longitude.toDouble()
            )

            points.add(position)

            val marker = Marker(map)
            marker.position = position
            marker.icon = resources.getDrawable(R.drawable.station_marker, null)
            marker.alpha = 0.8f
            marker.setOnMarkerClickListener { m, _ ->
                startActivity(Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q=${m.position.latitude},${m.position.longitude} (${station.name})")
                ))

                true
            }

            map.overlays.add(marker)
        }

        map.addOnFirstLayoutListener { _, _, _, _, _ ->
            map.zoomToBoundingBox(BoundingBox.fromGeoPoints(points), false)
        }

        this.map = map
    }


    override fun onResume() {
        super.onResume()

        map!!.onResume()
    }

    override fun onPause() {
        super.onPause()

        map!!.onPause()
    }

}
