package com.example.rescueapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PlannedRoute(
    val id: String,
    val name: String,
    val startLocation: LocationPoint,
    val endLocation: LocationPoint,
    val waypoints: List<LocationPoint> = emptyList(),
    val description: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Serializable
data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val address: String = ""
)
