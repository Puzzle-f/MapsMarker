package com.example.mapsmarker

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.mapsmarker.databinding.FragmentMarkersBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException

class MapFragment : Fragment(), OnMapReadyCallback, LocationListener {

    private var _binding: FragmentMarkersBinding? = null
    private val binding get() = _binding ?: throw RuntimeException("ViewBinding access error!")
    private var map: GoogleMap? = null
    private val textMarker = "Отмечено"
    private var isGPSEnabled = false
    private var isNetworkEnabled = false
    private var canGetLocation = false
    private var location: Location? = null
    private var locationManager: LocationManager? = null



    fun initButtonRemoveMarker(marker: Marker){
        binding.removeMarker.setOnClickListener {
            marker.remove()
        }
    }

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap
        val initialPlace = LatLng(52.52000659999999, 13.404953999999975)
        googleMap.addMarker(
            MarkerOptions().position(initialPlace).title(getString(R.string.marker_start))
        )
        googleMap.setOnMarkerClickListener {
            initButtonRemoveMarker(it)
            false
        }
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(initialPlace))
        googleMap.setOnMapLongClickListener { latLng ->
            googleMap.addMarker(
                MarkerOptions().position(latLng).title("хорошее место")
            )
        }
        activateMyLocation(googleMap)
    }

    private fun activateMyLocation(googleMap: GoogleMap) {
        context?.let {
            val isPermissionGranted =
                ContextCompat.checkSelfPermission(it,
                    Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED
            googleMap.isMyLocationEnabled = isPermissionGranted
            googleMap.uiSettings.isMyLocationButtonEnabled = isPermissionGranted
        }
    }


    @SuppressLint("MissingPermission")
    private fun getLocation(): Location? {
        try {
            locationManager = context?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            isGPSEnabled =
                locationManager?.isProviderEnabled(LocationManager.GPS_PROVIDER) ?: false
            isNetworkEnabled =
                locationManager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) ?: false
            if (!isGPSEnabled && !isNetworkEnabled) {
                // no network provider is enabled
            } else {
                canGetLocation = true
                if (isNetworkEnabled) {
                    if (ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(
                            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        ActivityCompat.requestPermissions(
                            (context as Activity?)!!,
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            ), 101
                        )
                    }
                    locationManager?.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES,
                        MIN_DISTANCE_CHANGE_FOR_UPDATES, this
                    )
                    location =
                        locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                }
                if (isGPSEnabled) {
                    if (location == null) {
                        if (ActivityCompat.checkSelfPermission(
                                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED &&
                            ActivityCompat.checkSelfPermission(
                                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            ActivityCompat.requestPermissions(
                                (context as Activity?)!!, arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                ), 101
                            )
                        }
                        locationManager?.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, this
                        )
                        location =
                            locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return location
    }

    @SuppressLint("MissingPermission")
    fun stopUsingGPS() {
        locationManager?.removeUpdates(this)
    }

    private fun showSettingsAlert() {
        AlertDialog.Builder(requireContext())
            .setTitle("Настройка GPS")
            .setMessage("GPS отключен. Вы хотите попасть в меню настроек?")
            .setPositiveButton("Настройки") { _, _ ->
                context?.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("Отмена") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMarkersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment =
            childFragmentManager.findFragmentById(binding.map.id) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }


    fun setMarker(location: LatLng, title: String){
        val marker = MarkerOptions()
            .position(location)
            .title(title)
        map!!.addMarker(marker)
    }

    private fun GoogleMap.goToLocation(point: LatLng, title: String) {
        this.addMarker(
            MarkerOptions()
                .position(point)
                .title(title)
        )
        this.moveCamera(CameraUpdateFactory.newLatLngZoom(point, MAP_ZOOM_ON_POSITION))
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map!!.setOnMapLongClickListener {
            setMarker(LatLng(it.latitude, it.longitude), textMarker)
            Log.d("", "** Vovan Tut **")
        }
        when (location) {
            null -> location = getLocation()
            else -> map?.goToLocation(
                LatLng(location!!.latitude, location!!.longitude),
                "Моё местоположение"
            )
        }
    }

    override fun onLocationChanged(location: Location) {
        this.location = location
        map?.goToLocation(LatLng(location.latitude, location.longitude), "Моё местоположение")
    }

    override fun onDestroyView() {
        stopUsingGPS()
        _binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance() = MapFragment()
        private const val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 10.0F // 10 meters
        private const val MIN_TIME_BW_UPDATES = (1000 * 60 * 1).toLong() // 1 minute
        private const val MAP_ZOOM_ON_POSITION: Float = 15.0F // 1 minute
    }
}