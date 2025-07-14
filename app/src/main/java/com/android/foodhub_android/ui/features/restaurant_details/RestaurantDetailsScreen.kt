package com.android.foodhub_android.ui.features.restaurant_details

import android.util.Log
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil3.compose.AsyncImage
import com.android.foodhub_android.R
import com.android.foodhub_android.data.models.FoodItem
import com.android.foodhub_android.ui.gridItems
import com.android.foodhub_android.ui.navigation.FoodDetails

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.RestaurantDetailsScreen(
    navController: NavController,
    name: String,
    imageUrl: String,
    restaurantID: String,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: RestaurantViewModel = hiltViewModel()
) {
    LaunchedEffect(restaurantID) {
        viewModel.getFoodItem(restaurantID)
    }

    val uiState = viewModel.uiState.collectAsState()
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {
            RestaurantDetailsHeader(
                imageUrl = imageUrl,
                restaurantID = restaurantID,
                animatedVisibilityScope = animatedVisibilityScope,
                onBackButton = { navController.popBackStack() },
                onFavouriteButton = {}
            )
        }
        item {
            RestaurantDetails(
                title = name,
                description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
                restaurantID = restaurantID,
                animatedVisibilityScope = animatedVisibilityScope
            )
        }
        when (uiState.value) {
            is RestaurantViewModel.RestaurantEvent.Loading -> {
                item {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Text(text = "Loading")
                    }
                }
            }

            is RestaurantViewModel.RestaurantEvent.Success -> {
                val foodItems =
                    (uiState.value as RestaurantViewModel.RestaurantEvent.Success).foodItems
                if (foodItems.isNotEmpty()) {
                    gridItems(foodItems, 2) { foodItem ->
                        FoodItemView(foodItem = foodItem,animatedVisibilityScope) {
                            navController.navigate(FoodDetails(foodItem))
                        }
                    }
                } else {
                    item {
                        Text(text = "No Food Items")
                    }
                }

            }

            is RestaurantViewModel.RestaurantEvent.Error -> {
                item {
                    Text(text = "Error")
                }
            }

            RestaurantViewModel.RestaurantEvent.Nothing -> {
                item {
                    Text(text = "Nothing")
                }
            }
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.RestaurantDetails(
    title: String,
    description: String,
    restaurantID: String,
    animatedVisibilityScope: AnimatedVisibilityScope
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.sharedElement(
                sharedContentState = rememberSharedContentState(key = "title/{$restaurantID}"),
                animatedVisibilityScope = animatedVisibilityScope
            )
        )
        Spacer(modifier = Modifier.size(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "4.5",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "(30)+",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.align(Alignment.CenterVertically)
            )
            Spacer(modifier = Modifier.size(8.dp))
            TextButton(onClick = {}) {
                Text(
                    text = "View All Reviews",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        Spacer(modifier = Modifier.size(8.dp))
        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium
        )

    }

}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.RestaurantDetailsHeader(
    imageUrl: String,
    restaurantID: String,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onBackButton: () -> Unit,
    onFavouriteButton: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        AsyncImage(
            model = imageUrl,
            contentDescription = "Restaurant Image",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .sharedElement(
                    sharedContentState = rememberSharedContentState(key = "image/{$restaurantID}"),
                    animatedVisibilityScope = animatedVisibilityScope
                )
                .clip(
                    RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                ),
            contentScale = ContentScale.Crop
        )
        IconButton(
            onClick = onBackButton,
            modifier = Modifier
                .align(Alignment.TopStart)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_back_button),
                contentDescription = null,
                modifier = Modifier.size(48.dp).padding(2.dp)
            )
        }
        IconButton(
            onClick = onFavouriteButton,
            modifier = Modifier
                .padding(2.dp)
                .size(58.dp)
                .align(Alignment.TopEnd)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_favourite),
                contentDescription = null
            )
        }
    }
}

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.FoodItemView(
    foodItem: FoodItem,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: (FoodItem) -> Unit
) {
    Column(
        modifier = Modifier
            .padding(8.dp)
            .width(162.dp)
            .height(216.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color.Gray.copy(alpha = 0.8f),
                spotColor = Color.Gray.copy(alpha = 0.8f)
            )
            .background(color = Color.White)
            .clickable { onClick.invoke(foodItem) }
            .clip(RoundedCornerShape(16.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(147.dp)
        ) {
            AsyncImage(
                model = foodItem.imageUrl,
                contentDescription = "Food Item Image",
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .sharedElement(
                        sharedContentState = rememberSharedContentState(key = "image/{${foodItem.id}}"),
                        animatedVisibilityScope = animatedVisibilityScope
                    ),
                contentScale = ContentScale.Crop
            )
            Text(
                text = "${foodItem.price}",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .padding(8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .padding(horizontal = 16.dp)
                    .align(Alignment.TopStart)
            )
            Image(
                painter = painterResource(R.drawable.ic_favourite),
                contentDescription = "Favourite Button",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .align(Alignment.TopEnd)
            )
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "4.5",
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = "(30)+",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    maxLines = 1,
                    modifier = Modifier.padding(end = 4.dp)
                )
            }
        }
        Column(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = foodItem.name,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                modifier = Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(key = "title/${foodItem.id}"),
                    animatedVisibilityScope
                )
            )
            Text(
                text = "$${foodItem.price}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                maxLines = 1
            )

        }

    }
}