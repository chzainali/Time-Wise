package com.example.timewise

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.timewise.databinding.ActivityMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Locale

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    var binding: ActivityMapBinding? = null
    private var mMap: GoogleMap? = null
    private var fusedLocationClient: FusedLocationProviderClient? = null
    private var placesClient: PlacesClient? = null
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        Places.initialize(applicationContext, "AIzaSyANUkNEMNuY8-Aa5hY0eSdUoQFkQn5J72s")
        placesClient = Places.createClient(this)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        binding?.ivBack?.setOnClickListener { finish() }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        } else {
            mMap!!.isMyLocationEnabled = true
            currentLocation
        }
    }

    private val currentLocation: Unit
        private get() {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            fusedLocationClient!!.lastLocation
                .addOnSuccessListener(this) { location: Location? ->
                    if (location != null) {
                        val currentLocation = LatLng(location.latitude, location.longitude)
                        mMap!!.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 10f))
                        mMap!!.addMarker(
                            MarkerOptions()
                                .position(currentLocation)
                                .title("You are here")
                        )
                        fetchNearbyGyms(currentLocation)
                    }
                }
        }

    @SuppressLint("StaticFieldLeak")
    private fun fetchNearbyGyms(location: LatLng) {
        val placesClient = Places.createClient(this)
        val locationString = location.latitude.toString() + "," + location.longitude
        val radius = "50000" // 5000 meters (adjust as needed)
        val type = "gym" // Type for gyms
        val keyword = "gym" // Keyword for gyms
        val apiKey = "AIzaSyD9-7VF_J-YFK2g8_UethYoeR73R_pNGtY"
        val url = String.format(
            "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%s&radius=%s&type=%s&keyword=%s&key=%s",
            locationString, radius, type, keyword, apiKey
        )
        object : AsyncTask<Void?, Void?, Void?>() {
            override fun doInBackground(vararg p0: Void?): Void? {
                try {
                    // Make an HTTP request and parse the response
                    val apiUrl = URL(url)
                    val urlConnection = apiUrl.openConnection() as HttpURLConnection
                    try {
                        val `in`: InputStream = BufferedInputStream(urlConnection.inputStream)
                        // Parse the JSON response and add markers for each gym
                        parseNearbyShopsResponse(`in`)
                    } finally {
                        urlConnection.disconnect()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                return null
            }

            override fun onPostExecute(aVoid: Void?) {
                // Handle any post-execution tasks if needed
            }
        }.execute()
    }

    private fun parseNearbyShopsResponse(inputStream: InputStream) {
        try {
            val reader = BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8))
            val responseStringBuilder = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                responseStringBuilder.append(line)
            }
            val response = JSONObject(responseStringBuilder.toString())
            if (response.has("results")) {
                val resultsArray = response.getJSONArray("results")

                // Limit the number of gyms to display
                val numberOfGyms = Math.min(resultsArray.length(), 5)

                // Create LatLngBounds to include all markers
                val boundsBuilder = LatLngBounds.Builder()
                for (i in 0 until numberOfGyms) {
                    val shopObject = resultsArray.getJSONObject(i)
                    val locationObject =
                        shopObject.getJSONObject("geometry").getJSONObject("location")
                    val lat = locationObject.getDouble("lat")
                    val lng = locationObject.getDouble("lng")
                    val gymLocation = LatLng(lat, lng)
                    val address = getAddressFromLocation(gymLocation)
                    runOnUiThread {
                        try {
                            val markerOptions = MarkerOptions()
                                .position(gymLocation)
                                .title(shopObject.getString("name"))
                                .snippet(address) // Set the address as snippet
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))

                            // Add marker to the map
                            val marker = mMap!!.addMarker(markerOptions)

                            // Include the marker in the bounds
                            boundsBuilder.include(gymLocation)

                            // Set a click listener for the marker
                            mMap!!.setOnMarkerClickListener { clickedMarker: Marker ->
                                // Show info window when marker is clicked
                                clickedMarker.showInfoWindow()

                                // Set a click listener for the info window
                                mMap!!.setOnInfoWindowClickListener(OnInfoWindowClickListener { marker -> // Open Google Maps with the specific location when info window is clicked
                                    openGoogleMaps(marker.position)
                                })
                                true
                            }
                        } catch (e: JSONException) {
                            throw RuntimeException(e)
                        }
                    }
                }

                // Build the LatLngBounds from the markers' positions
                val bounds = boundsBuilder.build()

                // Calculate a reasonable zoom level based on the distance between markers
                val padding = mapPadding // Set padding as needed (in pixels)
                val cu = CameraUpdateFactory.newLatLngBounds(bounds, padding)
                mMap!!.moveCamera(cu)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val mapPadding: Int
        // Helper method to calculate padding for the newLatLngBounds
        private get() =// Set padding as needed (in pixels)
            100

    // Helper method to get the address from the LatLng location
    private fun getAddressFromLocation(location: LatLng): String {
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!addresses!!.isEmpty()) {
                val address = addresses[0]
                return address.getAddressLine(0)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }

    // Helper method to open Google Maps with the specific location
    private fun openGoogleMaps(location: LatLng) {
        val gmmIntentUri =
            Uri.parse("geo:" + location.latitude + "," + location.longitude + "?q=" + location.latitude + "," + location.longitude)
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")
        startActivity(mapIntent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start location updates
                if (mMap != null) {
                    if (ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return
                    }
                    mMap!!.isMyLocationEnabled = true
                    currentLocation
                }
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}