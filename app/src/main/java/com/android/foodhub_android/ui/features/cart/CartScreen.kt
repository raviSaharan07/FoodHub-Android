package com.android.foodhub_android.ui.features.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.android.foodhub_android.R
import com.android.foodhub_android.data.models.Address
import com.android.foodhub_android.data.models.CartItem
import com.android.foodhub_android.data.models.CheckoutDetails
import com.android.foodhub_android.ui.BasicDialog
import com.android.foodhub_android.ui.features.food_item_details.FoodItemCounter
import com.android.foodhub_android.ui.navigation.AddressList
import com.android.foodhub_android.ui.navigation.OrderSuccess
import com.android.foodhub_android.ui.theme.Orange
import com.android.foodhub_android.utils.StringUtils
import com.stripe.android.PaymentConfiguration
import com.stripe.android.paymentsheet.PaymentSheet
import com.stripe.android.paymentsheet.PaymentSheetResult
import com.stripe.android.paymentsheet.rememberPaymentSheet
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(navController: NavController, viewModel: CartViewModel) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val showErrorDialog = remember { mutableStateOf(false) }

    val address = navController.currentBackStackEntry?.savedStateHandle?.getStateFlow<Address?>(
        "address",
        null
    )?.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = address?.value) {
        address?.value?.let {
            viewModel.onAddressSelected(it)
        }
    }

    val paymentSheet = rememberPaymentSheet(paymentResultCallback = {
        //Handle Payment Result
        if (it is PaymentSheetResult.Completed) {
            viewModel.onPaymentSuccess()
        } else {
            viewModel.onPaymentFailed()
        }
    })

    LaunchedEffect(key1 = true) {
        viewModel.event.collectLatest {
            when (it) {
                is CartViewModel.CartEvent.OnItemRemovedError,
                is CartViewModel.CartEvent.OnQuantityUpdateError,
                is CartViewModel.CartEvent.ShowErrorDialog -> {
                    showErrorDialog.value = true
                }

                is CartViewModel.CartEvent.OnAddressClicked -> {
                    navController.navigate(AddressList)
                }

                is CartViewModel.CartEvent.OrderSuccess -> {
                    navController.navigate(OrderSuccess(it.orderId!!))
                }

                is CartViewModel.CartEvent.OnInitiatePayment -> {
                    //Initiate Payment
                    PaymentConfiguration.init(
                        navController.context,
                        it.data.publishableKey
                    )
                    val customer = PaymentSheet.CustomerConfiguration(
                        it.data.customerId,
                        it.data.ephemeralKeySecret
                    )

                    val paymentSheetConfig = PaymentSheet.Configuration(
                        merchantDisplayName = "FoodHub",
                        customer = customer,
                        allowsDelayedPaymentMethods = false
                    )
                    paymentSheet.presentWithPaymentIntent(
                        it.data.paymentIntentClientSecret,
                        paymentSheetConfig
                    )
                }

                else -> {

                }


            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        CartHeaderView(onBack = { navController.popBackStack() })

        when (uiState.value) {
            is CartViewModel.CartUiState.Loading -> {
                Spacer(modifier = Modifier.size(16.dp))
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.size(16.dp))
                    CircularProgressIndicator()
                    Text(
                        text = "Loading...",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.Gray
                    )
                }
            }

            is CartViewModel.CartUiState.Success -> {
                val data = (uiState.value as CartViewModel.CartUiState.Success).data
                if (data.items.isNotEmpty()) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(680.dp)
                    ) {
                        items(data.items) {
                            CartItemView(
                                cartItem = it,
                                onIncrement = { cartItem, _ ->
                                    viewModel.incrementQuantity(cartItem)
                                },
                                onDecrement = { cartItem, _ ->
                                    viewModel.decrementQuantity(cartItem)
                                },
                                onRemove = { cartItem ->
                                    viewModel.removeItem(cartItem)
                                }
                            )
                        }
                        item {
                            CheckoutDetailsView(checkoutDetails = data.checkoutDetails)
                        }
                        item {
                            val selectedAddress =
                                viewModel.selectedAddress.collectAsStateWithLifecycle()
                            Spacer(modifier = Modifier.weight(1f))
                            if (uiState.value is CartViewModel.CartUiState.Success) {
                                AddressCard(selectedAddress.value) {
                                    viewModel.onAddressClicked()
                                }
                                Button(
                                    onClick = { viewModel.checkout() },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    enabled = selectedAddress.value != null

                                ) {
                                    Text(text = "Checkout")
                                }
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cart),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Orange
                        )
                        Text(
                            text = "No items in cart",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )
                    }
                }
            }

            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val message = (uiState.value as? CartViewModel.CartUiState.Error)?.message
                        ?: "Error Occurred"
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Button(onClick = {})
                    {
                        Text(text = "Retry")
                    }
                }
            }

        }
    }

    if (showErrorDialog.value) {
        ModalBottomSheet(onDismissRequest = { showErrorDialog.value = false }) {
            BasicDialog(
                title = viewModel.errorTitle,
                description = viewModel.errorMessage
            ) {
                showErrorDialog.value = false
            }
        }
    }
}

@Composable
fun AddressCard(address: Address?, onAddressClicked: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .shadow(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .clickable { onAddressClicked.invoke() }
            .padding(16.dp)
    ) {
        if (address != null) {
            Column {
                Text(
                    text = address.addressLine1 ?: "",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = "${address.city}, ${address.state}, ${address.country}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }
        } else {
            Text(
                text = "Select Address",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }

}

@Composable
fun CheckoutDetailsView(checkoutDetails: CheckoutDetails) {
    Column {
        CheckoutRowItem(
            title = "SubTotal",
            value = checkoutDetails.subTotal,
            currency = "USD"
        )
        CheckoutRowItem(title = "Tax", value = checkoutDetails.tax, currency = "USD")
        CheckoutRowItem(
            title = "Delivery Fee",
            value = checkoutDetails.deliveryFee,
            currency = "USD"
        )
        CheckoutRowItem(
            title = "Total",
            value = checkoutDetails.totalAmount,
            currency = "USD"
        )
    }
}

@Composable
fun CheckoutRowItem(title: String, value: Double, currency: String) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = StringUtils.formatCurrency(value),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = currency,
                style = MaterialTheme.typography.titleMedium,
                color = Color.LightGray
            )
        }
        VerticalDivider()
    }
}

@Composable
fun CartItemView(
    cartItem: CartItem,
    onIncrement: (CartItem, Int) -> Unit,
    onDecrement: (CartItem, Int) -> Unit,
    onRemove: (CartItem) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        AsyncImage(
            model = cartItem.menuItemId.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(82.dp)
                .clip(RoundedCornerShape(12.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.size(12.dp))
        Column(verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = cartItem.menuItemId.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { onRemove.invoke(cartItem) },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = cartItem.menuItemId.description,
                maxLines = 1,
                color = Color.Gray,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.size(8.dp))
            Row {
                Text(
                    text = "$${cartItem.menuItemId.price}",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.weight(1f))
                FoodItemCounter(
                    onCounterIncrement = { onIncrement.invoke(cartItem, cartItem.quantity) },
                    onCounterDecrement = { onDecrement.invoke(cartItem, cartItem.quantity) },
                    count = cartItem.quantity
                )
            }
        }
    }
}

@Composable
fun CartHeaderView(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp), // Set height if needed
        contentAlignment = Alignment.Center
    ) {
        // Centered Title
        Text(
            text = "Cart",
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )

        // Back Button aligned to the start
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(64.dp)
        ) {
            Image(
                painter = painterResource(R.drawable.ic_back_button),
                contentDescription = "Back",
                modifier = Modifier.size(48.dp)
            )
        }
    }
}
