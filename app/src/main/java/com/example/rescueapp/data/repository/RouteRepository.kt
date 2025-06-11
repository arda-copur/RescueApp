package com.example.rescueapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.rescueapp.data.model.PlannedRoute
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouteRepository @Inject constructor(
    private val context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("route_prefs", Context.MODE_PRIVATE)

    private val _plannedRoutes = MutableStateFlow<List<PlannedRoute>>(emptyList())
    val plannedRoutes: Flow<List<PlannedRoute>> = _plannedRoutes.asStateFlow()

    init {
        loadRoutes()
    }

    fun addRoute(route: PlannedRoute) {
        val currentRoutes = _plannedRoutes.value.toMutableList()
        currentRoutes.add(route)
        saveRoutes(currentRoutes)
        _plannedRoutes.value = currentRoutes
    }

    fun removeRoute(routeId: String) {
        val currentRoutes = _plannedRoutes.value.toMutableList()
        currentRoutes.removeAll { it.id == routeId }
        saveRoutes(currentRoutes)
        _plannedRoutes.value = currentRoutes
    }

    private fun saveRoutes(routes: List<PlannedRoute>) {
        val routesJson = Json.encodeToString(routes)
        sharedPreferences.edit()
            .putString("planned_routes", routesJson)
            .apply()
    }

    private fun loadRoutes() {
        val routesJson = sharedPreferences.getString("planned_routes", null)
        if (routesJson != null) {
            try {
                val routes = Json.decodeFromString<List<PlannedRoute>>(routesJson)
                _plannedRoutes.value = routes
            } catch (e: Exception) {
                _plannedRoutes.value = emptyList()
            }
        }
    }
}
