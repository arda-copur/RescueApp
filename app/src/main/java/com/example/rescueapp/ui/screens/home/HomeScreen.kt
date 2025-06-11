package com.example.rescueapp.ui.screens.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.rescueapp.components.StatusCard
import com.example.rescueapp.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToEmergency: () -> Unit,
    onNavigateToContacts: () -> Unit,
    onNavigateToRoutes: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    // Permission launchers
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            viewModel.onLocationPermissionGranted()
        }
    }

    val smsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onSmsPermissionResult(isGranted)
    }

    val contactsPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        viewModel.onContactsPermissionResult(isGranted)
    }

    // Emergency button animation
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val emergencyPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ), label = ""
    )

    LaunchedEffect(Unit) {
        viewModel.checkPermissions()
    }

    // Show permission dialog if needed
    if (uiState.showPermissionDialog) {
        PermissionDialog(
            permissionType = uiState.permissionDialogType,
            onGrantPermission = {
                when (uiState.permissionDialogType) {
                    "location" -> {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                    "sms" -> {
                        smsPermissionLauncher.launch(Manifest.permission.SEND_SMS)
                    }
                    "contacts" -> {
                        contactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                    }
                }
                viewModel.dismissPermissionDialog()
            },
            onDismiss = { viewModel.dismissPermissionDialog() }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "RescueMe",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            "Güvenlik Asistanınız",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = { viewModel.toggleLocationTracking() }) {
                        Icon(
                            if (uiState.isLocationTrackingEnabled) Icons.Default.LocationOn else Icons.Default.Close,
                            contentDescription = "Konum Takibi",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { /* Settings */ }) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Ayarlar",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                // Emergency Button
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(emergencyPulse),
                    shape = RoundedCornerShape(24.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
                    onClick = onNavigateToEmergency
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(
                                        Color.White.copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "ACİL DURUM",
                                color = Color.White,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                "Kayboldum - Yardım Et",
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            item {
                // Status Cards
                Text(
                    "Durum Bilgileri",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatusCard(
                        title = "Konum Takibi",
                        value = if (uiState.isLocationTrackingEnabled && uiState.hasLocationPermission) "Aktif" else "Pasif",
                        icon = Icons.Default.LocationOn,
                        isActive = uiState.isLocationTrackingEnabled && uiState.hasLocationPermission
                    )

                    StatusCard(
                        title = "Son Güncelleme",
                        value = uiState.lastLocationUpdate,
                        icon = Icons.Default.Notifications,
                        isActive = uiState.isLocationTrackingEnabled && uiState.hasLocationPermission
                    )

                    StatusCard(
                        title = "Acil Durum Kişileri",
                        value = "${uiState.emergencyContactsCount} kişi",
                        icon = Icons.Default.Person,
                        isActive = uiState.emergencyContactsCount > 0
                    )

                    StatusCard(
                        title = "Planlanan Rotalar",
                        value = "${uiState.plannedRoutesCount} rota",
                        icon = Icons.Default.LocationOn,
                        isActive = uiState.plannedRoutesCount > 0
                    )
                }
            }

            item {
                // Quick Actions
                Text(
                    "Hızlı İşlemler",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "Yardım Listesi",
                        subtitle = "${uiState.emergencyContactsCount} kişi",
                        icon = Icons.Default.Person,
                        onClick = {
                            if (uiState.hasContactsPermission) {
                                onNavigateToContacts()
                            } else {
                                viewModel.requestContactsPermission()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        gradient = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.secondary,
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.8f)
                            )
                        )
                    )

                    QuickActionCard(
                        title = "Rotalarım",
                        subtitle = "${uiState.plannedRoutesCount} rota",
                        icon = Icons.Default.LocationOn,
                        onClick = {
                            if (uiState.hasLocationPermission) {
                                onNavigateToRoutes()
                            } else {
                                viewModel.requestLocationPermission()
                            }
                        },
                        modifier = Modifier.weight(1f),
                        gradient = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.tertiary,
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                            )
                        )
                    )
                }
            }

            item {
                // Permissions Status
                if (!uiState.hasLocationPermission || !uiState.hasSmsPermission || !uiState.hasContactsPermission) {
                    PermissionsStatusCard(
                        hasLocationPermission = uiState.hasLocationPermission,
                        hasSmsPermission = uiState.hasSmsPermission,
                        hasContactsPermission = uiState.hasContactsPermission,
                        onRequestLocationPermission = { viewModel.requestLocationPermission() },
                        onRequestSmsPermission = { viewModel.requestSmsPermission() },
                        onRequestContactsPermission = { viewModel.requestContactsPermission() }
                    )
                }
            }

            item {
                // Safety Tips
                SafetyTipsCard()
            }
        }
    }
}

@Composable
fun PermissionDialog(
    permissionType: String,
    onGrantPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    val (title, description, icon) = when (permissionType) {
        "location" -> Triple(
            "Konum İzni Gerekli",
            "Acil durumlarda konumunuzu paylaşabilmek için konum izni vermeniz gerekiyor.",
            Icons.Default.LocationOn
        )
        "sms" -> Triple(
            "SMS İzni Gerekli",
            "Acil durumlarda yardım kişilerinize SMS gönderebilmek için SMS izni vermeniz gerekiyor.",
            Icons.Default.Call
        )
        "contacts" -> Triple(
            "Rehber İzni Gerekli",
            "Rehberinizden acil durum kişilerini seçebilmek için rehber izni vermeniz gerekiyor.",
            Icons.Default.Person
        )
        else -> Triple("İzin Gerekli", "Bu özelliği kullanmak için izin gerekli.", Icons.Default.Build)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                title,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(description)
        },
        confirmButton = {
            TextButton(onClick = onGrantPermission) {
                Text("İzin Ver")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Şimdi Değil")
            }
        }
    )
}

@Composable
fun PermissionsStatusCard(
    hasLocationPermission: Boolean,
    hasSmsPermission: Boolean,
    hasContactsPermission: Boolean,
    onRequestLocationPermission: () -> Unit,
    onRequestSmsPermission: () -> Unit,
    onRequestContactsPermission: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Eksik İzinler",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!hasLocationPermission) {
                PermissionItem(
                    title = "Konum İzni",
                    description = "Acil durumlarda konum paylaşımı için",
                    icon = Icons.Default.LocationOn,
                    onClick = onRequestLocationPermission
                )
            }

            if (!hasSmsPermission) {
                PermissionItem(
                    title = "SMS İzni",
                    description = "Acil durum mesajları için",
                    icon = Icons.Default.Email,
                    onClick = onRequestSmsPermission
                )
            }

            if (!hasContactsPermission) {
                PermissionItem(
                    title = "Rehber İzni",
                    description = "Acil durum kişilerini seçmek için",
                    icon = Icons.Default.Person,
                    onClick = onRequestContactsPermission
                )
            }
        }
    }
}

@Composable
fun PermissionItem(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        TextButton(onClick = onClick) {
            Text("Ver", fontSize = 12.sp)
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: Brush
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(gradient)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )

                Column {
                    Text(
                        title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                    Text(
                        subtitle,
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun SafetyTipsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Notifications,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Güvenlik İpuçları",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val tips = listOf(
                "Bilinmeyen yerlere giderken rotanızı önceden planlayın",
                "Acil durum kişilerinizi güncel tutun",
                "Telefonunuzun şarjını kontrol edin",
                "Yakınlarınıza nereye gittiğinizi bildirin"
            )

            tips.forEach { tip ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                CircleShape
                            )
                            .padding(top = 8.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        tip,
                        fontSize = 14.sp,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
