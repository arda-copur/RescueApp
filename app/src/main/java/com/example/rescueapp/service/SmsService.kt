package com.example.rescueapp.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.rescueapp.data.model.LocationPoint
import com.example.rescueapp.data.repository.ContactRepository
import com.example.rescueapp.data.repository.RouteRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmsService @Inject constructor(
    private val context: Context,
    private val contactRepository: ContactRepository,
    private val routeRepository: RouteRepository
) {
    private val smsManager = SmsManager.getDefault()

    fun sendEmergencyLocationSMS(location: LocationPoint) {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("SmsService", "SMS permission not granted")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val contacts = contactRepository.emergencyContacts.first()
                val routes = routeRepository.plannedRoutes.first()

                if (contacts.isEmpty()) {
                    Log.w("SmsService", "No emergency contacts found")
                    return@launch
                }

                val message = buildEmergencyMessage(location, routes)

                contacts.forEach { contact ->
                    try {
                        // For long messages, split into multiple parts
                        val parts = smsManager.divideMessage(message)
                        if (parts.size == 1) {
                            smsManager.sendTextMessage(
                                contact.phoneNumber,
                                null,
                                message,
                                null,
                                null
                            )
                        } else {
                            smsManager.sendMultipartTextMessage(
                                contact.phoneNumber,
                                null,
                                parts,
                                null,
                                null
                            )
                        }
                        Log.d("SmsService", "Emergency SMS sent to ${contact.name}")
                    } catch (e: Exception) {
                        Log.e("SmsService", "Failed to send SMS to ${contact.name}: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("SmsService", "Error in sendEmergencyLocationSMS: ${e.message}")
            }
        }
    }

    private fun buildEmergencyMessage(
        location: LocationPoint,
        routes: List<com.example.rescueapp.data.model.PlannedRoute>
    ): String {
        val message = StringBuilder()
        message.append("🚨 ACİL DURUM - KAYBOLDUM!\n\n")
        message.append("Bu mesaj RescueMe uygulaması tarafından otomatik gönderilmiştir.\n\n")
        message.append("Güncel Konumum:\n")
        message.append("Enlem: ${String.format("%.6f", location.latitude)}\n")
        message.append("Boylam: ${String.format("%.6f", location.longitude)}\n")
        message.append("Google Maps: https://maps.google.com/?q=${location.latitude},${location.longitude}\n\n")

        if (routes.isNotEmpty()) {
            message.append("Planlanan Rotalarım:\n")
            routes.take(2).forEach { route ->
                message.append("📍 ${route.name}")
                if (route.description.isNotBlank()) {
                    message.append(": ${route.description}")
                }
                message.append("\n")
                message.append("   Başlangıç: ${String.format("%.4f", route.startLocation.latitude)}, ${String.format("%.4f", route.startLocation.longitude)}\n")
                message.append("   Bitiş: ${String.format("%.4f", route.endLocation.latitude)}, ${String.format("%.4f", route.endLocation.longitude)}\n")
            }
            message.append("\n")
        }

        message.append("⚠️ LÜTFEN YARDIM EDİN!\n")
        message.append("Bu acil durum mesajıdır. Mümkünse arayın veya yetkililere haber verin.")

        return message.toString()
    }

    fun sendTestSMS(phoneNumber: String): Boolean {
        return try {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.SEND_SMS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }

            val testMessage = "RescueMe test mesajı. Uygulama düzgün çalışıyor."
            smsManager.sendTextMessage(phoneNumber, null, testMessage, null, null)
            Log.d("SmsService", "Test SMS sent successfully")
            true
        } catch (e: Exception) {
            Log.e("SmsService", "Failed to send test SMS: ${e.message}")
            false
        }
    }
}
