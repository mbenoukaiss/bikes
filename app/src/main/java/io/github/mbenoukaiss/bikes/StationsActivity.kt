package io.github.mbenoukaiss.bikes

import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import androidx.preference.PreferenceManager
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
import kotlin.collections.ArrayList


class StationsActivity : Activity() {

    companion object {
        const val LOCATION_REQUEST: Int = 17872
    }

    private var map: MapView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stations)

        //set the toolbar to the city name
        val title = findViewById<TextView>(R.id.toolbar_title)
        title.text = intent.getStringExtra("CITY_NAME")

        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))

        val map = findViewById<MapView>(R.id.map)
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.minZoomLevel = 5.5

        //load markers
        val stations = intent.getSerializableExtra("STATIONS") as ArrayList<Station>
        addMarkers(map, stations)

        val myPosition = findViewById<FloatingActionButton>(R.id.my_position)
        myPosition.setOnClickListener {
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
            val lm = getSystemService(Context.LOCATION_SERVICE) as LocationManager

            goToPosition(map!!, lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)!!)
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

    /**
     * Adds markers for each station on the map.
     */
    private fun addMarkers(map: MapView, stations: ArrayList<Station>) {
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
                spawnStationPopup(station, station.position)
                true
            }

            map.overlays.add(marker)
        }

        //zoom to see all the markers
        map.addOnFirstLayoutListener { _, _, _, _, _ ->
            map.zoomToBoundingBox(BoundingBox.fromGeoPoints(points), false)
        }
    }

    /**
     * Shows the bottom popup with station details when
     * a user clicks on a station.
     */
    private fun spawnStationPopup(st: Station, position: Position) {
        val builder = AlertDialog.Builder(this)
        val layout: View = View.inflate(this, R.layout.popup_station, null)

        val dialog: AlertDialog = builder.create()
        val window = dialog.window!!

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        dialog.setView(layout, 0, 0, 0, 0)
        dialog.setCanceledOnTouchOutside(true)
        dialog.setCancelable(true)

        window.attributes.gravity = Gravity.BOTTOM

        builder.setView(layout)

        val stands = String.format(resources.getString(R.string.available_stands), st.totalStands.availabilities.stands)
        val bikes = String.format(resources.getString(R.string.available_bikes), st.totalStands.availabilities.bikes)

        layout.findViewById<TextView>(R.id.name).text = st.name
        layout.findViewById<TextView>(R.id.address).text = st.address
        layout.findViewById<TextView>(R.id.available_stands).text = stands
        layout.findViewById<TextView>(R.id.available_bikes).text = bikes

        val go = layout.findViewById<Button>(R.id.go_to_station)
        go.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("geo:0,0?q=${position.latitude},${position.longitude} (${st.name})")
                )
            )
        }

        dialog.show()
    }

    /**
     * Moves the map to the GPS coordinates of the phone.
     */
    private fun goToPosition(map: MapView, location: Location) {
        val controller = map.controller

        val point = GeoPoint(location.latitude, location.longitude)
        val position = Marker(map)
        position.position = point
        position.icon = resources.getDrawable(R.drawable.ic_marker, null)
        position.setAnchor(0.5f, 1f)
        position.setOnMarkerClickListener { _, _ -> true }

        map.overlays.add(position)

        controller.zoomTo(18.5)
        controller.animateTo(point)
    }

}
