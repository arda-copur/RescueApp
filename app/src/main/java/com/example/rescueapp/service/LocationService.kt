package com.example.rescueapp.service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.rescueapp.MainActivity
import com.example.rescueapp.R
import com.example.rescueapp.data.model.LocationPoint
import com.example.rescueapp.data.repository.LocationRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocationService : Service(), LocationListener {

    @Inject
    lateinit var locationRepository: LocationRepository

    @Inject
    lateinit var smsService: SmsService

    private lateinit var locationManager: LocationManager
    private var lastKnownLocation: Location? = null
    private var isEmergencyActive = false

    companion object {
        const val CHANNEL_ID = "LocationServiceChannel"
        const val NOTIFICATION_ID = 1
        private const val LOCATION_UPDATE_INTERVAL = 30000L // 30 seconds
        private const val LOCATION_UPDATE_DISTANCE = 50f // 50 meters
        private const val EMERGENCY_DISTANCE_THRESHOLD = 100f // 100 meters
    }

    override fun onCreate() {
        super.onCreate()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        createNotificationChannel()
        isEmergencyActive = locationRepository.getEmergencyStatus()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())
        startLocationUpdates()
        return START_STICKY
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "Konum Takip Servisi",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "RescueMe konum takip servisi"
                setShowBadge(false)
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
                if (isEmergencyActive) "Acil durum aktif - Konum takip ediliyor"
                else "Konum güvenli bir şekilde takip ediliyor"
            )
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
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

        try {
            // Request location updates from GPS provider
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    LOCATION_UPDATE_INTERVAL,
                    LOCATION_UPDATE_DISTANCE,
                    this
                )
                Log.d("LocationService", "GPS location updates started")
            }

            // Request location updates from Network provider as backup
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    LOCATION_UPDATE_INTERVAL,
                    LOCATION_UPDATE_DISTANCE,
                    this
                )
                Log.d("LocationService", "Network location updates started")
            }

            // Get last known location immediately
            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            lastLocation?.let { onLocationChanged(it) }

        } catch (e: Exception) {
            Log.e("LocationService", "Error starting location updates: ${e.message}")
        }
    }

    override fun onLocationChanged(location: Location) {
        Log.d("LocationService", "Location updated: ${location.latitude}, ${location.longitude}")

        val locationPoint = LocationPoint(
            latitude = location.latitude,
            longitude = location.longitude
        )

        // Always save the current location
        locationRepository.saveCurrentLocation(locationPoint)

        // Check if emergency is active and location changed significantly
        val currentEmergencyStatus = locationRepository.getEmergencyStatus()
        if (currentEmergencyStatus != isEmergencyActive) {
            isEmergencyActive = currentEmergencyStatus
            // Update notification
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.notify(NOTIFICATION_ID, createNotification())
        }

        if (isEmergencyActive) {
            val shouldSendSMS = lastKnownLocation?.let { lastLoc ->
                location.distanceTo(lastLoc) > EMERGENCY_DISTANCE_THRESHOLD
            } ?: true // Send SMS for first location in emergency

            if (shouldSendSMS) {
                Log.d("LocationService", "Sending emergency SMS due to location change")
                smsService.sendEmergencyLocationSMS(locationPoint)
            }
        }

        lastKnownLocation = location
    }

    override fun onProviderEnabled(provider: String) {
        Log.d("LocationService", "Provider enabled: $provider")
    }

    override fun onProviderDisabled(provider: String) {
        Log.d("LocationService", "Provider disabled: $provider")
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            locationManager.removeUpdates(this)
            Log.d("LocationService", "Location updates stopped")
        } catch (e: Exception) {
            Log.e("LocationService", "Error stopping location updates: ${e.message}")
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
