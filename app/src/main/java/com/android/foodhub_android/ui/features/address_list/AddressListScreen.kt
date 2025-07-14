package com.android.foodhub_android.ui.features.address_list

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.android.foodhub_android.R
import com.android.foodhub_android.ui.features.cart.AddressCard
import com.android.foodhub_android.ui.navigation.AddAddress
import com.android.foodhub_android.ui.theme.Orange
import kotlinx.coroutines.flow.collectLatest

@Composable
fun AddressListScreen(
    navController: NavController,
    viewModel: AddressListViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = true) {
        viewModel.event.collectLatest {
            when (val addressEvent = it) {
                is AddressListViewModel.AddressEvent.NavigateToEditAddress -> {
                }

                is AddressListViewModel.AddressEvent.NavigateToAddAddress -> {
                    navController.navigate(AddAddress)
                }
                is AddressListViewModel.AddressEvent.NavigateBack -> {
                    val address = addressEvent.address
                    navController.previousBackStackEntry?.savedStateHandle?.set("address", address)
                    navController.popBackStack()
                }

                else -> {

                }

            }
        }
    }

    val isAddressAdded =
        navController.currentBackStackEntry?.savedStateHandle?.getStateFlow("isAddressAdded", false)?.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = isAddressAdded?.value) {
        if (isAddressAdded?.value == true) {
            viewModel.getAddress()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_back_button),
                contentDescription = null,
                modifier = Modifier
                    .padding(16.dp)
                    .size(48.dp)
                    .clickable { navController.popBackStack() }
            )
            Text(
                text = "Address List",
                style = TextStyle(fontSize = 20.sp)
            )
            Icon(
                imageVector = Icons.Default.AddCircle,
                contentDescription = null,
                modifier = Modifier
                    .padding(16.dp)
                    .size(32.dp)
                    .clickable { viewModel.onAddAddressClicked() },
                tint = Orange
            )
        }

        when (val addressState = state.value) {
            is AddressListViewModel.AddressState.Loading -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                    Text(
                        text = "Loading...",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                }
            }

            is AddressListViewModel.AddressState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                        .fillMaxSize()
                ) {
                    items(addressState.data) { address ->
                        AddressCard(address = address, onAddressClicked = {
                            viewModel.onAddressSelected(address)
                        })
                    }
                }
            }

            is AddressListViewModel.AddressState.Error -> {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "Error: ${addressState.message}",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                    Button(onClick = { viewModel.getAddress() }) {
                        Text(
                            text = "Retry",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }


}