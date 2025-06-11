package com.example.rescueapp.ui.screens.contacts

import android.Manifest
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String
)

@Composable
fun ContactPickerDialog(
    onDismiss: () -> Unit,
    onContactSelected: (Contact, String) -> Unit
) {
    val context = LocalContext.current
    var contacts by remember { mutableStateOf<List<Contact>>(emptyList()) }
    var hasPermission by remember { mutableStateOf(false) }
    var selectedContact by remember { mutableStateOf<Contact?>(null) }
    var relationship by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
        if (isGranted) {
            contacts = getContacts(context)
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rehberden Kişi Seç") },
        text = {
            Column {
                if (!hasPermission) {
                    Text("Rehber erişimi için izin gerekli")
                } else if (selectedContact == null) {
                    LazyColumn(
                        modifier = Modifier.height(300.dp)
                    ) {
                        items(contacts) { contact ->
                            ContactItem(
                                contact = contact,
                                onClick = { selectedContact = contact }
                            )
                        }
                    }
                } else {
                    Column {
                        Text(
                            "Seçilen Kişi: ${selectedContact!!.name}",
                            fontWeight = FontWeight.Bold
                        )
                        Text("Telefon: ${selectedContact!!.phoneNumber}")
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = relationship,
                            onValueChange = { relationship = it },
                            label = { Text("Yakınlık (Aile, Arkadaş, vb.)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (selectedContact != null && relationship.isNotBlank()) {
                TextButton(
                    onClick = {
                        onContactSelected(selectedContact!!, relationship)
                    }
                ) {
                    Text("Ekle")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}

@Composable
fun ContactItem(
    contact: Contact,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    contact.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    contact.phoneNumber,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun getContacts(context: Context): List<Contact> {
    val contacts = mutableListOf<Contact>()
    val cursor: Cursor? = context.contentResolver.query(
        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
        arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        ),
        null,
        null,
        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
    )

    cursor?.use {
        val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
        val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
        val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

        while (it.moveToNext()) {
            val id = it.getString(idIndex)
            val name = it.getString(nameIndex)
            val number = it.getString(numberIndex)

            if (name != null && number != null) {
                contacts.add(Contact(id, name, number.replace("\\s".toRegex(), "")))
            }
        }
    }

    return contacts.distinctBy { it.phoneNumber }
}
