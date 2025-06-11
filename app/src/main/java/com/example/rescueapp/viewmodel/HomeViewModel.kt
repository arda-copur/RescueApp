package com.example.rescueapp.viewmodel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rescueapp.data.repository.ContactRepository
import com.example.rescueapp.data.repository.LocationRepository
import com.example.rescueapp.data.repository.RouteRepository
import com.example.rescueapp.service.LocationTrackingService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class HomeUiState(
    val isLocationTrackingEnabled: Boolean = false,
    val lastLocationUpdate: String = "Henüz güncellenmedi",
    val emergencyContactsCount: Int = 0,
    val plannedRoutesCount: Int = 0,
    val hasLocationPermission: Boolean = false,
    val hasSmsPermission: Boolean = false,
    val hasContactsPermission: Boolean = false,
    val showPermissionDialog: Boolean = false,
    val permissionDialogType: String = ""
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationRepository: LocationRepository,
    private val contactRepository: ContactRepository,
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        observeData()
        checkPermissions()
    }

    private fun observeData() {
        viewModelScope.launch {
            combine(
                locationRepository.currentLocation,
                contactRepository.emergencyContacts,
                routeRepository.plannedRoutes
            ) { location, contacts, routes ->
                val lastUpdate = locationRepository.getLastLocationUpdate()
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                _uiState.value = _uiState.value.copy(
                    lastLocationUpdate = if (lastUpdate > 0) {
                        dateFormat.format(Date(lastUpdate))
                    } else {
                        "Henüz güncellenmedi"
                    },
                    emergencyContactsCount = contacts.size,
                    plannedRoutesCount = routes.size
                )
            }
        }
    }

    fun checkPermissions() {
        val hasLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasSmsPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        val hasContactsPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.READ_CONTACTS
        ) == PackageManager.PERMISSION_GRANTED

        _uiState.value = _uiState.value.copy(
            hasLocationPermission = hasLocationPermission,
            hasSmsPermission = hasSmsPermission,
            hasContactsPermission = hasContactsPermission,
            isLocationTrackingEnabled = hasLocationPermission && locationRepository.getCurrentLocation() != null
        )

        // Start location tracking if permission is granted
        if (hasLocationPermission) {
            startLocationTracking()
        }
    }

    private fun startLocationTracking() {
        LocationTrackingService.startService(context)

        // Update current location if available
        val currentLocation = locationRepository.getCurrentLocation()
        if (currentLocation != null) {
            locationRepository.saveCurrentLocation(currentLocation)
        }

        _uiState.value = _uiState.value.copy(isLocationTrackingEnabled = true)
    }

    fun toggleLocationTracking() {
        if (_uiState.value.hasLocationPermission) {
            if (_uiState.value.isLocationTrackingEnabled) {
                LocationTrackingService.stopService(context)
                _uiState.value = _uiState.value.copy(isLocationTrackingEnabled = false)
            } else {
                startLocationTracking()
            }
        } else {
            requestLocationPermission()
        }
    }

    fun requestLocationPermission() {
        _uiState.value = _uiState.value.copy(
            showPermissionDialog = true,
            permissionDialogType = "location"
        )
    }

    fun requestSmsPermission() {
        _uiState.value = _uiState.value.copy(
            showPermissionDialog = true,
            permissionDialogType = "sms"
        )
    }

    fun requestContactsPermission() {
        _uiState.value = _uiState.value.copy(
            showPermissionDialog = true,
            permissionDialogType = "contacts"
        )
    }

    fun dismissPermissionDialog() {
        _uiState.value = _uiState.value.copy(showPermissionDialog = false)
    }

    fun onLocationPermissionGranted() {
        _uiState.value = _uiState.value.copy(hasLocationPermission = true)
        startLocationTracking()
    }

    fun onSmsPermissionResult(isGranted: Boolean) {
        _uiState.value = _uiState.value.copy(hasSmsPermission = isGranted)
    }

    fun onContactsPermissionResult(isGranted: Boolean) {
        _uiState.value = _uiState.value.copy(hasContactsPermission = isGranted)
    }
}
