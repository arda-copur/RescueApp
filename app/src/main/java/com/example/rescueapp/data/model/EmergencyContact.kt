package com.example.rescueapp.data.model

import kotlinx.serialization.Serializable

@Serializable
data class EmergencyContact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val relationship: String
)
