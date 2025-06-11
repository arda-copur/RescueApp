package com.example.rescueapp.ui.screens.routes

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.rescueapp.components.GradientButton
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapsRouteScreen(
    onNavigateBack: () -> Unit,
    onRouteSaved: (String, LatLng, LatLng, List<LatLng>, String) -> Unit
) {
    val context = LocalContext.current
    var mapView by remember { mutableStateOf<MapView?>(null) }
    var googleMap by remember { mutableStateOf<GoogleMap?>(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var routePoints by remember { mutableStateOf<List<LatLng>>(emptyList()) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var isSelectingRoute by remember { mutableStateOf(false) }
    var currentPolyline by remember { mutableStateOf<Polyline?>(null) }

    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasLocationPermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Rota Planlama",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                actions = {
                    if (routePoints.size >= 2) {
                        IconButton(onClick = { showSaveDialog = true }) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Kaydet",
                                tint = Color.White
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            routePoints = emptyList()
                            googleMap?.clear()
                            currentPolyline = null
                            isSelectingRoute = false
                        }
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Temizle",
                            tint = Color.White
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasLocationPermission) {
                AndroidView(
                    factory = { context ->
                        MapView(context).apply {
                            onCreate(null)
                            onResume()
                            getMapAsync { map ->
                                googleMap = map
                                setupMap(map, fusedLocationClient) { latLng ->
                                    if (isSelectingRoute) {
                                        routePoints = routePoints + latLng
                                        addMarkerToMap(map, latLng, routePoints.size)
                                        if (routePoints.size > 1) {
                                            currentPolyline?.remove()
                                            currentPolyline = drawRoute(map, routePoints)
                                        }
                                    }
                                }
                            }
                            mapView = this
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Bottom control panel
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Rota Oluşturma",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            if (routePoints.isEmpty()) {
                                "Rota oluşturmaya başlamak için 'Rota Seç' butonuna basın"
                            } else {
                                "Seçilen nokta sayısı: ${routePoints.size} • ${
                                    if (isSelectingRoute) "Haritaya dokunarak nokta ekleyin"
                                    else "Rota seçimi durduruldu"
                                }"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            GradientButton(
                                text = if (isSelectingRoute) "Seçimi Durdur" else "Rota Seç",
                                onClick = { isSelectingRoute = !isSelectingRoute },
                                modifier = Modifier.weight(1f),
                                icon = if (isSelectingRoute) Icons.Default.Close else Icons.Default.PlayArrow
                            )

                            if (routePoints.size >= 2) {
                                GradientButton(
                                    text = "Kaydet",
                                    onClick = { showSaveDialog = true },
                                    modifier = Modifier.weight(1f),
                                    icon = Icons.Default.Check
                                )
                            }
                        }
                    }
                }
            } else {
                // Permission request UI
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(32.dp),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "Konum İzni Gerekli",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Harita üzerinde rota oluşturmak için konum izni vermeniz gerekiyor.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        GradientButton(
                            text = "İzin Ver",
                            onClick = {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            icon = Icons.Default.LocationOn
                        )
                    }
                }
            }
        }
    }

    if (showSaveDialog && routePoints.size >= 2) {
        SaveRouteDialog(
            onDismiss = { showSaveDialog = false },
            onSave = { name, description ->
                val startPoint = routePoints.first()
                val endPoint = routePoints.last()
                val waypoints = if (routePoints.size > 2) {
                    routePoints.subList(1, routePoints.size - 1)
                } else {
                    emptyList()
                }
                onRouteSaved(name, startPoint, endPoint, waypoints, description)
                showSaveDialog = false
            }
        )
    }
}

private fun setupMap(
    map: GoogleMap,
    fusedLocationClient: FusedLocationProviderClient,
    onMapClick: (LatLng) -> Unit
) {
    map.uiSettings.apply {
        isZoomControlsEnabled = true
        isMyLocationButtonEnabled = true
        isCompassEnabled = true
        isMapToolbarEnabled = false
    }

    try {
        map.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                val currentLocation = LatLng(it.latitude, it.longitude)
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(currentLocation, 16f)
                )
            }
        }
    } catch (e: SecurityException) {
        // Handle permission error
    }

    map.setOnMapClickListener { latLng ->
        onMapClick(latLng)
    }
}

private val markerList = mutableListOf<Marker>() // Marker'ları saklamak için liste

private fun addMarkerToMap(map: GoogleMap, latLng: LatLng, index: Int) {
    val markerOptions = MarkerOptions()
        .position(latLng)
        .title("Nokta $index")
        .icon(
            BitmapDescriptorFactory.defaultMarker(
                when (index) {
                    1 -> BitmapDescriptorFactory.HUE_GREEN // Başlangıç
                    markerList.size + 1 -> BitmapDescriptorFactory.HUE_RED // Bitiş (son marker)
                    else -> BitmapDescriptorFactory.HUE_BLUE // Ara noktalar
                }
            )
        )

    // Marker'ı haritaya ekleyip listeye kaydediyoruz
    val marker = map.addMarker(markerOptions)
    marker?.let { markerList.add(it) }
}
private fun drawRoute(map: GoogleMap, points: List<LatLng>): Polyline? {
    if (points.size < 2) return null

    val polylineOptions = PolylineOptions()
        .addAll(points)
        .color(android.graphics.Color.BLUE)
        .width(8f)
        .pattern(listOf(Dot(), Gap(10f)))

    return map.addPolyline(polylineOptions)
}

@Composable
fun SaveRouteDialog(
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var routeName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Rotayı Kaydet",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = routeName,
                    onValueChange = { routeName = it },
                    label = { Text("Rota Adı") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Açıklama (Opsiyonel)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (routeName.isNotBlank()) {
                        onSave(routeName, description)
                    }
                },
                enabled = routeName.isNotBlank()
            ) {
                Text("Kaydet")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
