package com.example.rescueapp.service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.rescueapp.MainActivity
import com.example.rescueapp.R
import com.example.rescueapp.data.model.LocationPoint
import com.example.rescueapp.data.repository.LocationRepository
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : Service() {

    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var smsService: SmsService

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var lastKnownLocation: Location? = null
    private var isEmergencyActive = false

    companion object {
        const val CHANNEL_ID = "LocationTrackingChannel"
        const val NOTIFICATION_ID = 1001
        private const val LOCATION_UPDATE_INTERVAL = 15000L // 15 seconds
        private const val LOCATION_UPDATE_FASTEST_INTERVAL = 10000L // 10 seconds
        private const val EMERGENCY_DISTANCE_THRESHOLD = 50f // 50 meters

        fun startService(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, LocationTrackingService::class.java)
            context.stopService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createNotificationChannel()
        setupLocationCallback()
        isEmergencyActive = locationRepository.getEmergencyStatus()
        Log.d("LocationService", "Service created")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startLocationUpdates()
        Log.d("LocationService", "Service started")
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Konum Takip Servisi",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "RescueMe konum takip servisi çalışıyor"
                setShowBadge(false)
                setSound(null, null)
            }

            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("RescueMe Aktif")
            .setContentText(
                if (isEmergencyActive) "🚨 Acil durum aktif - Konum takip ediliyor"
                else "📍 Konum güvenli bir şekilde takip ediliyor"
            )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun setupLocationCallback() {
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    handleLocationUpdate(location)
                }
            }

            override fun onLocationAvailability(locationAvailability: LocationAvailability) {
                Log.d("LocationService", "Location availability: ${locationAvailability.isLocationAvailable}")
            }
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("LocationService", "Location permissions not granted")
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            LOCATION_UPDATE_INTERVAL
        ).apply {
            setMinUpdateIntervalMillis(LOCATION_UPDATE_FASTEST_INTERVAL)
            setMinUpdateDistanceMeters(10f)
            setMaxUpdateDelayMillis(LOCATION_UPDATE_INTERVAL * 2)
        }.build()

        try {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
            Log.d("LocationService", "Location updates started")

            // Get last known location immediately
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let { handleLocationUpdate(it) }
            }

        } catch (e: Exception) {
            Log.e("LocationService", "Error starting location updates: ${e.message}")
        }
    }

    private fun handleLocationUpdate(location: Location) {
        Log.d("LocationService", "Location updated: ${location.latitude}, ${location.longitude}, accuracy: ${location.accuracy}")

        val locationPoint = LocationPoint(
            latitude = location.latitude,
            longitude = location.longitude
        )

        // Always save the current location
        locationRepository.saveCurrentLocation(locationPoint)

        // Check if emergency status changed
        val currentEmergencyStatus = locationRepository.getEmergencyStatus()
        if (currentEmergencyStatus != isEmergencyActive) {
            isEmergencyActive = currentEmergencyStatus
            updateNotification()
            Log.d("LocationService", "Emergency status changed: $isEmergencyActive")
        }

        // Send SMS if emergency is active and location changed significantly
        if (isEmergencyActive) {
            val shouldSendSMS = lastKnownLocation?.let { lastLoc ->
                val distance = location.distanceTo(lastLoc)
                Log.d("LocationService", "Distance from last location: ${distance}m")
                distance > EMERGENCY_DISTANCE_THRESHOLD
            } ?: true // Send SMS for first location in emergency

            if (shouldSendSMS) {
                Log.d("LocationService", "Sending emergency SMS due to location change")
                smsService.sendEmergencyLocationSMS(locationPoint)
            }
        }

        lastKnownLocation = location
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            fusedLocationClient.removeLocationUpdates(locationCallback)
            Log.d("LocationService", "Location updates stopped")
        } catch (e: Exception) {
            Log.e("LocationService", "Error stopping location updates: ${e.message}")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
