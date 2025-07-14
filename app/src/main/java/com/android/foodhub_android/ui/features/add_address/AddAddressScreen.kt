package com.android.foodhub_android.ui.features.add_address

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.android.foodhub_android.ui.features.cart.AddressCard
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter

@Composable
fun AddAddressScreen(
    navController: NavController,
    viewModel: AddAddressViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = true) {
        viewModel.event.collectLatest {
            when (it) {
                is AddAddressViewModel.AddAddressEvent.NavigateToAddressList -> {
                    Toast.makeText(
                        navController.context,
                        "Address Added Successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    navController.previousBackStackEntry?.savedStateHandle?.set(
                        "isAddressAdded",
                        true
                    )
                    navController.popBackStack()
                }

            }
        }
    }

    val isPermissionGranted = remember { mutableStateOf(false) }
    RequestLocationPermission(
        onPermissionGranted = {
            isPermissionGranted.value = true
            viewModel.getLocation()
        },
        onPermissionRejected = {
            Toast.makeText(navController.context, "Permission Denied", Toast.LENGTH_SHORT).show()
            navController.popBackStack()
        }
    )
    if (!isPermissionGranted.value) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            val location = viewModel.getLocation().collectAsStateWithLifecycle(initialValue = null)
            location.value?.let {
                val cameraState = rememberCameraPositionState()
                LaunchedEffect(key1 = Unit) {
                    cameraState.position =
                        CameraPosition.fromLatLngZoom(LatLng(it.latitude, it.longitude), 13f)
                }

                val centerScreenMarker = remember {
                    mutableStateOf(LatLng(it.latitude, it.longitude))
                }

                LaunchedEffect(key1 = cameraState) {
                    snapshotFlow {
                        cameraState.position.target
                    }.collectLatest {
                        centerScreenMarker.value = cameraState.position.target
                        if (!cameraState.isMoving) {
                            viewModel.reverseGeocode(
                                centerScreenMarker.value.latitude,
                                centerScreenMarker.value.longitude
                            )
                        }
                    }
                }
                GoogleMap(
                    cameraPositionState = cameraState,
                    modifier = Modifier.fillMaxSize(),
                    uiSettings = MapUiSettings(
                        zoomControlsEnabled = true,
                        myLocationButtonEnabled = true,
                        compassEnabled = true
                    ),
                    properties = MapProperties(
                        isMyLocationEnabled = true
                    )
                ) {
                    centerScreenMarker.value.let {
                        Marker(
                            state = MarkerState(position = LatLng(it.latitude, it.longitude))
                        )
                    }
                }
            }
            val address = viewModel.address.collectAsStateWithLifecycle()
            address.value?.let {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .shadow(8.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .clickable { }
                        .padding(16.dp)
                        .align(Alignment.BottomCenter)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        when (uiState.value) {
                            is AddAddressViewModel.AddAddressState.AddressStoring -> {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            }

                            is AddAddressViewModel.AddAddressState.Error -> {
                                Text(
                                    text = (uiState.value as AddAddressViewModel.AddAddressState.Error).message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Red
                                )
                            }

                            else -> {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = it.addressLine1 ?: "",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Spacer(modifier = Modifier.size(4.dp))
                                    Text(
                                        text = "${it.city}, ${it.state}, ${it.country}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                }
                                Button(onClick = { viewModel.onAddAddressClicked() }) {
                                    Text(text = "Add Address")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RequestLocationPermission(onPermissionGranted: () -> Unit, onPermissionRejected: () -> Unit) {
    val context = LocalContext.current

    if (context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
        context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    ) {
        onPermissionGranted()
        return
    }

    val permission = listOf(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        if (result.values.all { it }) {
            onPermissionGranted()
        } else {
            onPermissionRejected()
        }
    }
    LaunchedEffect(key1 = Unit) {
        permissionLauncher.launch(permission.toTypedArray())
    }

}