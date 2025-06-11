package com.example.rescueapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rescueapp.data.model.LocationPoint
import com.example.rescueapp.data.repository.LocationRepository
import com.example.rescueapp.service.SmsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class EmergencyUiState(
    val isEmergencyActive: Boolean = false,
    val currentLocation: LocationPoint? = null,
    val lastUpdate: String = ""
)

@HiltViewModel
class EmergencyViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val smsService: SmsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(EmergencyUiState())
    val uiState: StateFlow<EmergencyUiState> = _uiState.asStateFlow()

    init {
        observeEmergencyStatus()
    }

    private fun observeEmergencyStatus() {
        viewModelScope.launch {
            locationRepository.isEmergencyActive.collect { isActive ->
                val currentLocation = locationRepository.getCurrentLocation()
                val lastUpdate = locationRepository.getLastLocationUpdate()
                val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

                _uiState.value = EmergencyUiState(
                    isEmergencyActive = isActive,
                    currentLocation = currentLocation,
                    lastUpdate = if (lastUpdate > 0) {
                        dateFormat.format(Date(lastUpdate))
                    } else {
                        "Bilinmiyor"
                    }
                )
            }
        }
    }

    fun activateEmergency() {
        viewModelScope.launch {
            locationRepository.setEmergencyStatus(true)
            val currentLocation = locationRepository.getCurrentLocation()
            if (currentLocation != null) {
                smsService.sendEmergencyLocationSMS(currentLocation)
            }
        }
    }

    fun deactivateEmergency() {
        viewModelScope.launch {
            locationRepository.setEmergencyStatus(false)
        }
    }
}
