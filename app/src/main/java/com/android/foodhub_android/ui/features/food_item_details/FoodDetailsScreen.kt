package com.android.foodhub_android.ui.features.food_item_details

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.content.MediaType.Companion.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.android.foodhub_android.R
import com.android.foodhub_android.data.models.FoodItem
import com.android.foodhub_android.ui.BasicDialog
import com.android.foodhub_android.ui.features.auth.signup.SignUpViewModel
import com.android.foodhub_android.ui.features.restaurant_details.RestaurantDetails
import com.android.foodhub_android.ui.features.restaurant_details.RestaurantDetailsHeader
import com.android.foodhub_android.ui.navigation.Cart
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class)
@Composable
fun SharedTransitionScope.FoodDetailsScreen(
    navController: NavController,
    foodItem: FoodItem,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onItemAddedToCart: () -> Unit,
    viewModel: FoodDetailsViewModel = hiltViewModel()
) {
    val showSuccessDialog = remember { mutableStateOf(false) }
    val showErrorDialog = remember { mutableStateOf(false) }

    val count = viewModel.quantity.collectAsStateWithLifecycle()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val isLoading = remember { mutableStateOf(false) }

    when (uiState.value) {
        is FoodDetailsViewModel.FoodDetailsUiState.Loading -> {
            isLoading.value = true
        }

        is FoodDetailsViewModel.FoodDetailsUiState.Success -> {
            isLoading.value = false
        }

        else -> {
            isLoading.value = false
        }
    }


    LaunchedEffect(Unit) {
        viewModel.event.collectLatest {
            when (it) {
                is FoodDetailsViewModel.FoodDetailsEvent.OnAddToCard -> {
                    showSuccessDialog.value = true
                    onItemAddedToCart.invoke()
                }

                is FoodDetailsViewModel.FoodDetailsEvent.ShowErrorDialog -> {
                    showErrorDialog.value = true
                }

                is FoodDetailsViewModel.FoodDetailsEvent.GoToCart -> {
                    navController.navigate(Cart)
                }
            }
        }
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        RestaurantDetailsHeader(
            imageUrl = foodItem.imageUrl,
            restaurantID = foodItem.id,
            animatedVisibilityScope = animatedVisibilityScope,
            onBackButton = { navController.popBackStack() },
            onFavouriteButton = {}
        )
        RestaurantDetails(
            title = foodItem.name,
            description = foodItem.description,
            restaurantID = foodItem.id,
            animatedVisibilityScope = animatedVisibilityScope
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$${foodItem.price}",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.weight(1f))
            FoodItemCounter(
                onCounterIncrement = { viewModel.incrementQuantity() },
                onCounterDecrement = { viewModel.decrementQuantity() },
                count = count.value
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Button(
            onClick = {
                viewModel.addToCart(
                    restaurantId = foodItem.restaurantId,
                    foodItemId = foodItem.id
                )
            },
            enabled = !isLoading.value,
            modifier = Modifier
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .clip(RoundedCornerShape(32.dp)),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(visible = !isLoading.value) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.background(MaterialTheme.colorScheme.primary)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_cart),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = "Add to Cart".uppercase(),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                AnimatedVisibility(visible = isLoading.value) {
                    CircularProgressIndicator(
                        modifier = Modifier
                            .size(16.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }

    if (showSuccessDialog.value) {
        ModalBottomSheet(onDismissRequest = { showSuccessDialog.value = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Item added to cart",
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.size(16.dp))
                Button(
                    onClick = {
                        showSuccessDialog.value = false
                        viewModel.goToCart()
                    }, modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = "Go to cart")
                }
                Button(
                    onClick = {
                        showSuccessDialog.value = false
                    }, modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                ) {
                    Text(text = "Ok")
                }
            }
        }
    }

    if (showErrorDialog.value) {
        ModalBottomSheet(onDismissRequest = { showErrorDialog.value = false }) {
            BasicDialog(
                title = "Error",
                description = (uiState.value as? FoodDetailsViewModel.FoodDetailsUiState.Error)?.message
                    ?: "Failed to add to cart"
            ) {
                showErrorDialog.value = false
            }
        }
    }
}

@Composable
fun FoodItemCounter(onCounterIncrement: () -> Unit,
                    onCounterDecrement: () -> Unit,
                    count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Image(
            painter = painterResource(id = R.drawable.ic_add),
            contentDescription = null,
            modifier = Modifier
                .clickable { onCounterIncrement.invoke() }
                .clip(CircleShape)
        )
        Spacer(modifier = Modifier.size(4.dp))
        Text(
            text = "$count",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.size(4.dp))
        Image(
            painter = painterResource(id = R.drawable.ic_minus),
            contentDescription = null,
            modifier = Modifier
                .clickable { onCounterDecrement.invoke() }
                .clip(CircleShape)
        )
    }
}