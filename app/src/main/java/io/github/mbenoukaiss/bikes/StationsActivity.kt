package io.github.mbenoukaiss.bikes

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.github.mbenoukaiss.bikes.models.Position
import io.github.mbenoukaiss.bikes.models.Station
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker


const val LOCATION_REQUEST: Int = 17872

class StationsActivity : Activity() {

    private var map: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stations)

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        val map = findViewById<MapView>(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.minZoomLevel = 5.5

        //set the toolbar to the city name
        val title = findViewById<TextView>(R.id.toolbar_title)
        title.text = intent.getStringExtra("CITY_NAME")

        val stations = intent.getSerializableExtra("STATIONS") as ArrayList<Station>
        val points = ArrayList<GeoPoint>()

        for (station in stations) {
            val position = GeoPoint(
                station.position.latitude,
                station.position.longitude
            )

            points.add(position)

            val marker = Marker(map)
            marker.position = position
            marker.icon = resources.getDrawable(R.drawable.station_marker, null)
            marker.alpha = 0.8f
            marker.setOnMarkerClickListener { m, _ ->
                spawnStationPopup(station.name, station.address, station.position)
                true
            }

            map.overlays.add(marker)
        }

        map.addOnFirstLayoutListener { _, _, _, _, _ ->
            map.zoomToBoundingBox(BoundingBox.fromGeoPoints(points), false)
        }

        findViewById<FloatingActionButton>(R.id.my_position).setOnClickListener {
            requestPermissions(arrayOf(ACCESS_FINE_LOCATION), LOCATION_REQUEST)
        }

        this.map = map
    }

    private fun hasPermission(perm: String): Boolean {
        return PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm)
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(code: Int, perms: Array<out String>, res: IntArray) {
        if (code == LOCATION_REQUEST && hasPermission(ACCESS_FINE_LOCATION)) {
            val controller = map!!.controller
            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)

            controller.animateTo(GeoPoint(location!!.latitude, location!!.longitude))
        }
    }

    override fun onResume() {
        super.onResume()
        map!!.onResume()
    }

    override fun onPause() {
        super.onPause()
        map!!.onPause()
    }

    fun spawnStationPopup(name: String, address: String, position: Position) {
        val builder = AlertDialog.Builder(this)
        val layout: View = layoutInflater.inflate(R.layout.popup_station, null)

        val dialog: AlertDialog = builder.create()
        val window = dialog.window!!

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        dialog.setView(layout, 0, 0, 0, 0)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        window.attributes.gravity = Gravity.BOTTOM

        builder.setView(layout)

        layout.findViewById<TextView>(R.id.name).text = name
        layout.findViewById<TextView>(R.id.address).text = address

        val go = layout.findViewById<Button>(R.id.go_to_station)
        go.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q=${position.latitude},${position.longitude} ($name)")
                )
            )
        }

        dialog.show()
    }

}
