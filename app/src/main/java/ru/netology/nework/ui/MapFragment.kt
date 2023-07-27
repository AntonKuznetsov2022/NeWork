package ru.netology.nework.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.yandex.mapkit.Animation
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.layers.ObjectEvent
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.InputListener
import com.yandex.mapkit.map.Map
import com.yandex.mapkit.map.MapObjectCollection
import com.yandex.mapkit.mapview.MapView
import com.yandex.mapkit.user_location.UserLocationLayer
import com.yandex.mapkit.user_location.UserLocationObjectListener
import com.yandex.mapkit.user_location.UserLocationView
import com.yandex.runtime.ui_view.ViewProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import ru.netology.nework.R
import ru.netology.nework.databinding.FragmentMapBinding
import ru.netology.nework.ui.dialog.SaveCoordsDialog

@AndroidEntryPoint
@ExperimentalCoroutinesApi
class MapFragment : Fragment() {
    lateinit var binding: FragmentMapBinding
    private lateinit var mapView: MapView
    private lateinit var userLocationLayer: UserLocationLayer
    private lateinit var mapObjectCollection: MapObjectCollection

    companion object {
        private const val zoom = 14f
        private const val azimuth = 0F
        private const val tilt = 0F
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            when {
                granted -> {
                    userLocationLayer.isVisible = true
                    userLocationLayer.isHeadingEnabled = false
                    cameraUserPosition()
                }

                else -> {
                    Toast.makeText(requireContext(), R.string.permission, Toast.LENGTH_SHORT).show()
                }
            }
        }

    private val locationObjectListener = object : UserLocationObjectListener {
        override fun onObjectAdded(view: UserLocationView) = Unit

        override fun onObjectRemoved(view: UserLocationView) = Unit

        override fun onObjectUpdated(view: UserLocationView, event: ObjectEvent) {
            userLocationLayer.cameraPosition()?.target?.let {
                mapView.map?.move(CameraPosition(it, zoom, azimuth, tilt))
            }
            userLocationLayer.setObjectListener(null)
        }
    }

    private val listener = object : InputListener {
        override fun onMapTap(map: Map, point: Point) = Unit

        override fun onMapLongTap(map: Map, point: Point) {
            if (arguments?.getString("see") == null) {
                val value = arguments?.getString("open")
                SaveCoordsDialog.newInstance(point.latitude, point.longitude, value)
                    .show(childFragmentManager, null)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapKitFactory.initialize(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMapBinding.inflate(layoutInflater)

        binding.myLocation.setOnClickListener {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        onMapReady()

        mapView = binding.mapview.apply {
            map.addInputListener(listener)
        }

        val lat = arguments?.getDouble("lat")
        val long = arguments?.getDouble("long")

        if (lat != null && lat != 0.0 && long != null && long != 0.0) {
            mapView.map.mapObjects.clear()
            moveTo(Point(lat, long), zoom, azimuth, tilt)
            addMarker(Point(lat, long))
        } else {
            userLocationLayer.setObjectListener(locationObjectListener)
        }

        return binding.root
    }

    private fun onMapReady() {
        val mapKit = MapKitFactory.getInstance()
        mapObjectCollection = binding.mapview.map.mapObjects.addCollection()
        userLocationLayer = mapKit.createUserLocationLayer(binding.mapview.mapWindow)
        if (requireActivity().checkSelfPermission(
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            userLocationLayer.isVisible = true
            userLocationLayer.isHeadingEnabled = false
        }
    }

    private fun addMarker(point: Point) {
        val marker = View(context).apply {
            background =
                AppCompatResources.getDrawable(context, R.drawable.ic_location_on)
        }
        mapView.map.mapObjects.addPlacemark(point, ViewProvider(marker))
    }

    private fun cameraUserPosition() {
        if (userLocationLayer.cameraPosition() != null) {

            binding.mapview.map.move(
                CameraPosition(userLocationLayer.cameraPosition()!!.target, zoom, azimuth, tilt),
                Animation(Animation.Type.SMOOTH, 1f),
                null
            )
        } else {
            Snackbar.make(binding.root, R.string.error_location, Snackbar.LENGTH_SHORT).show()
        }
    }

    fun moveTo(point: Point, zoom: Float, azimuth: Float, tilt: Float) {
        binding.mapview.map.move(
            CameraPosition(point, zoom, azimuth, tilt),
            Animation(Animation.Type.SMOOTH, 2f),
            null
        )
    }

    override fun onStart() {
        super.onStart()
        binding.mapview.onStart()
        MapKitFactory.getInstance().onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapview.onStop()
        MapKitFactory.getInstance().onStop()
    }
}