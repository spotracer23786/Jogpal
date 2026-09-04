package com.jogpal.app.features.sos

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SOSRepository private constructor() {
    private val _trustedContacts = MutableStateFlow<List<TrustedContact>>(
        listOf(
            TrustedContact(id = "1", name = "Arun", phoneNumber = "+1 (555) 234-5678", relationship = "Friend", isEnabled = true),
            TrustedContact(id = "2", name = "Mom", phoneNumber = "+1 (555) 987-6543", relationship = "Family", isEnabled = true),
            TrustedContact(id = "3", name = "David", phoneNumber = "+1 (555) 456-7890", relationship = "Partner", isEnabled = false)
        )
    )
    val trustedContacts: StateFlow<List<TrustedContact>> = _trustedContacts.asStateFlow()

    private val _emergencyProfile = MutableStateFlow(
        EmergencyProfile(
            primaryContactName = "Arun",
            primaryContactPhone = "+1 (555) 234-5678",
            preferredLanguage = "English",
            importantNotes = "Prefers SMS alerts over phone calls. Speaks English & Spanish.",
            medicalInfo = "Asthma (Carries Inhaler), No known drug allergies"
        )
    )
    val emergencyProfile: StateFlow<EmergencyProfile> = _emergencyProfile.asStateFlow()

    private val _sosHistory = MutableStateFlow<List<SOSEvent>>(
        listOf(
            SOSEvent(title = "SOS Test", dateString = "Today · 7:42 PM", type = SOSEventType.TEST_MODE, contactsNotifiedCount = 0),
            SOSEvent(title = "Cancelled SOS", dateString = "Aug 28 · 6:21 PM", type = SOSEventType.CANCELLED_SOS, contactsNotifiedCount = 0),
            SOSEvent(title = "Active Emergency SOS", dateString = "Jul 14 · 8:15 AM", type = SOSEventType.ACTIVE_SOS, contactsNotifiedCount = 2)
        )
    )
    val sosHistory: StateFlow<List<SOSEvent>> = _sosHistory.asStateFlow()

    private val _inactivityCheckEnabled = MutableStateFlow(true)
    val inactivityCheckEnabled: StateFlow<Boolean> = _inactivityCheckEnabled.asStateFlow()

    fun addTrustedContact(contact: TrustedContact) {
        _trustedContacts.value = _trustedContacts.value + contact
    }

    fun toggleTrustedContact(id: String) {
        _trustedContacts.value = _trustedContacts.value.map {
            if (it.id == id) it.copy(isEnabled = !it.isEnabled) else it
        }
    }

    fun removeTrustedContact(id: String) {
        _trustedContacts.value = _trustedContacts.value.filter { it.id != id }
    }

    fun updateEmergencyProfile(profile: EmergencyProfile) {
        _emergencyProfile.value = profile
    }

    fun toggleInactivityCheck(enabled: Boolean) {
        _inactivityCheckEnabled.value = enabled
    }

    fun logSOSEvent(event: SOSEvent) {
        _sosHistory.value = listOf(event) + _sosHistory.value
    }

    companion object {
        @Volatile
        private var INSTANCE: SOSRepository? = null

        fun getInstance(): SOSRepository {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SOSRepository().also { INSTANCE = it }
            }
        }
    }
}
