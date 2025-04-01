package com.johncolani.solomoncompass

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var sensorManager: SensorManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var compassView: CompassView

    private val viewModel: CompassViewModel by viewModels()

    // Sensor data buffers
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    // Location constants
    private val jerusalemLat = 31.7683
    private val jerusalemLon = 35.2137
    private val locationRequest = LocationRequest.Builder(
        Priority.PRIORITY_HIGH_ACCURACY,
        5000
    ).build()

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) startLocationUpdates()
        else Log.e("MainActivity", "Location permission denied")
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            result.lastLocation?.let { location ->
                viewModel.targetBearing = calculateBearing(
                    location.latitude,
                    location.longitude
                )
                updateCompass()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeComponents()
        setupVideoPlayer()
        setupViewPager()
        checkPermissions()
    }

    private fun initializeComponents() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        compassView = findViewById(R.id.compass_view)

        registerSensors()
    }

    private fun registerSensors() {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
        sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun setupVideoPlayer() {
        val playerView: PlayerView = findViewById(R.id.video_player)
        exoPlayer = ExoPlayer.Builder(this).build().apply {
            setMediaItem(MediaItem.fromUri("asset:///solomon_temple.mp4"))
            prepare()
            playWhenReady = true
            repeatMode = ExoPlayer.REPEAT_MODE_ALL
        }
        playerView.player = exoPlayer
    }

    private fun setupViewPager() {
        val viewPager: ViewPager2 = findViewById(R.id.view_pager)
        val tabLayout: TabLayout = findViewById(R.id.tab_layout)

        val prayers = listOf(
            getString(R.string.prayer_1),
            getString(R.string.prayer_2),
            getString(R.string.prayer_3),
            getString(R.string.prayer_4)
        )

        viewPager.adapter = PrayerAdapter(prayers)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = "${getString(R.string.prayer)} ${position + 1}"
        }.attach()
    }

    private fun checkPermissions() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> startLocationUpdates()

            else -> locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun startLocationUpdates() {
        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        } catch (e: SecurityException) {
            Log.e("MainActivity", "Location permission revoked", e)
        }
    }

    private fun calculateBearing(userLat: Double, userLon: Double): Float {
        val φ1 = userLat.toRadians()
        val φ2 = jerusalemLat.toRadians()
        val Δλ = (jerusalemLon - userLon).toRadians()

        val y = sin(Δλ) * cos(φ2)
        val x = cos(φ1) * sin(φ2) - sin(φ1) * cos(φ2) * cos(Δλ)
        return ((atan2(y, x).toDegrees() + 360) % 360).toFloat()
    }

    private fun updateCompass() {
        val direction = (viewModel.targetBearing - viewModel.currentHeading + 360) % 360
        compassView.direction = direction
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> System.arraycopy(
                event.values, 0,
                accelerometerReading, 0, 3
            )
            Sensor.TYPE_MAGNETIC_FIELD -> System.arraycopy(
                event.values, 0,
                magnetometerReading, 0, 3
            )
        }

        if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)) {
            SensorManager.getOrientation(rotationMatrix, orientationAngles)

            // This converts the azimuth (orientationAngles[0]) from radians to degrees,
            // ensures positive value (0-360), and updates the ViewModel
            viewModel.currentHeading = (orientationAngles[0].toDouble().toDegrees() + 360).toFloat() % 360

            // Then update the compass display
            updateCompass()
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onPause() {
        super.onPause()
        exoPlayer.pause()
        sensorManager.unregisterListener(this)
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onResume() {
        super.onResume()
        exoPlayer.play()
        registerSensors()
        checkPermissions()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
        sensorManager.unregisterListener(this)
    }

    // Extension functions for cleaner math conversions
    private fun Double.toRadians() = Math.toRadians(this)
    private fun Double.toDegrees() = Math.toDegrees(this)
}