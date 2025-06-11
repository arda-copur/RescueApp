package com.example.rescueapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rescueapp.data.model.LocationPoint
import com.example.rescueapp.data.model.PlannedRoute
import com.example.rescueapp.data.repository.RouteRepository
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class RoutesUiState(
    val routes: List<PlannedRoute> = emptyList()
)

@HiltViewModel
class RoutesViewModel @Inject constructor(
    private val routeRepository: RouteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutesUiState())
    val uiState: StateFlow<RoutesUiState> = _uiState.asStateFlow()

    init {
        observeRoutes()
    }

    private fun observeRoutes() {
        viewModelScope.launch {
            routeRepository.plannedRoutes.collect { routes ->
                _uiState.value = RoutesUiState(routes = routes)
            }
        }
    }

    fun addRouteFromMap(
        name: String,
        startLatLng: LatLng,
        endLatLng: LatLng,
        waypointsLatLng: List<LatLng>,
        description: String
    ) {
        val route = PlannedRoute(
            id = UUID.randomUUID().toString(),
            name = name,
            startLocation = LocationPoint(startLatLng.latitude, startLatLng.longitude),
            endLocation = LocationPoint(endLatLng.latitude, endLatLng.longitude),
            waypoints = waypointsLatLng.map { LocationPoint(it.latitude, it.longitude) },
            description = description
        )
        routeRepository.addRoute(route)
    }

    fun addRoute(
        name: String,
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double,
        description: String
    ) {
        val route = PlannedRoute(
            id = UUID.randomUUID().toString(),
            name = name,
            startLocation = LocationPoint(startLat, startLng),
            endLocation = LocationPoint(endLat, endLng),
            description = description
        )
        routeRepository.addRoute(route)
    }

    fun removeRoute(routeId: String) {
        routeRepository.removeRoute(routeId)
    }
}
