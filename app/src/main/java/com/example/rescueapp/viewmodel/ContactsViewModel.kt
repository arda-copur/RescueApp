package com.example.rescueapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.rescueapp.data.model.EmergencyContact
import com.example.rescueapp.data.repository.ContactRepository
import com.example.rescueapp.service.SmsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ContactsUiState(
    val contacts: List<EmergencyContact> = emptyList()
)

@HiltViewModel
class ContactsViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val smsService: SmsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(ContactsUiState())
    val uiState: StateFlow<ContactsUiState> = _uiState.asStateFlow()

    init {
        observeContacts()
    }

    private fun observeContacts() {
        viewModelScope.launch {
            contactRepository.emergencyContacts.collect { contacts ->
                _uiState.value = ContactsUiState(contacts = contacts)
            }
        }
    }

    fun addContact(name: String, phoneNumber: String, relationship: String) {
        contactRepository.addContact(name, phoneNumber, relationship)
    }

    fun removeContact(contactId: String) {
        contactRepository.removeContact(contactId)
    }

    fun sendTestSMS(phoneNumber: String) {
        viewModelScope.launch {
            smsService.sendTestSMS(phoneNumber)
        }
    }
}
