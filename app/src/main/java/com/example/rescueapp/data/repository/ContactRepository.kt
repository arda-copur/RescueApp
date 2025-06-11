package com.example.rescueapp.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.rescueapp.data.model.EmergencyContact
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContactRepository @Inject constructor(
    private val context: Context
) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("contact_prefs", Context.MODE_PRIVATE)

    private val _emergencyContacts = MutableStateFlow<List<EmergencyContact>>(emptyList())
    val emergencyContacts: Flow<List<EmergencyContact>> = _emergencyContacts.asStateFlow()

    init {
        loadContacts()
    }

    fun addContact(name: String, phoneNumber: String, relationship: String) {
        val contact = EmergencyContact(
            id = UUID.randomUUID().toString(),
            name = name,
            phoneNumber = phoneNumber,
            relationship = relationship
        )

        val currentContacts = _emergencyContacts.value.toMutableList()
        currentContacts.add(contact)
        saveContacts(currentContacts)
        _emergencyContacts.value = currentContacts
    }

    fun removeContact(contactId: String) {
        val currentContacts = _emergencyContacts.value.toMutableList()
        currentContacts.removeAll { it.id == contactId }
        saveContacts(currentContacts)
        _emergencyContacts.value = currentContacts
    }

    private fun saveContacts(contacts: List<EmergencyContact>) {
        val contactsJson = Json.encodeToString(contacts)
        sharedPreferences.edit()
            .putString("emergency_contacts", contactsJson)
            .apply()
    }

    private fun loadContacts() {
        val contactsJson = sharedPreferences.getString("emergency_contacts", null)
        if (contactsJson != null) {
            try {
                val contacts = Json.decodeFromString<List<EmergencyContact>>(contactsJson)
                _emergencyContacts.value = contacts
            } catch (e: Exception) {
                _emergencyContacts.value = emptyList()
            }
        }
    }
}
