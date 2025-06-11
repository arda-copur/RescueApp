package com.example.rescueapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.rescueapp.data.model.LocationPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("location_prefs", Context.MODE_PRIVATE)

    private val _currentLocation = MutableStateFlow<LocationPoint?>(null)
    val currentLocation: Flow<LocationPoint?> = _currentLocation.asStateFlow()

    private val _isEmergencyActive = MutableStateFlow(false)
    val isEmergencyActive: Flow<Boolean> = _isEmergencyActive.asStateFlow()

    init {
        // Load initial data
        loadInitialData()
    }

    private fun loadInitialData() {
        val savedLocation = getCurrentLocation()
        _currentLocation.value = savedLocation

        val emergencyStatus = getEmergencyStatus()
        _isEmergencyActive.value = emergencyStatus
    }

    fun saveCurrentLocation(location: LocationPoint) {
        val locationJson = Json.encodeToString(location)
        sharedPreferences.edit()
            .putString("current_location", locationJson)
            .putLong("last_update", System.currentTimeMillis())
            .apply()
        _currentLocation.value = location
    }

    fun getCurrentLocation(): LocationPoint? {
        val locationJson = sharedPreferences.getString("current_location", null)
        return locationJson?.let {
            try {
                Json.decodeFromString<LocationPoint>(it)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun setEmergencyStatus(isActive: Boolean) {
        sharedPreferences.edit()
            .putBoolean("emergency_active", isActive)
            .apply()
        _isEmergencyActive.value = isActive
    }

    fun getEmergencyStatus(): Boolean {
        return sharedPreferences.getBoolean("emergency_active", false)
    }

    fun getLastLocationUpdate(): Long {
        return sharedPreferences.getLong("last_update", 0)
    }
}
