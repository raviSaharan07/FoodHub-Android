package com.android.foodhub_android.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.foodhub_android.data.FoodApi
import com.android.foodhub_android.data.models.Category
import com.android.foodhub_android.data.models.Restaurant
import com.android.foodhub_android.data.remote.ApiResponse
import com.android.foodhub_android.data.remote.safeApiCall
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class HomeScreenViewModel @Inject constructor(private val foodApi: FoodApi) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeScreenState>(HomeScreenState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<HomeScreenNavigationEvents?>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    var categories = emptyList<Category>()
    var restaurant = emptyList<Restaurant>()

    init {
        viewModelScope.launch {
            categories = getCategories()
            restaurant = getPopularRestaurants()

            if (categories.isNotEmpty() && restaurant.isNotEmpty()) {
                _uiState.value = HomeScreenState.Success
            } else {
                _uiState.value = HomeScreenState.Empty
            }
        }
    }

    private suspend fun getCategories(): List<Category> {
        var list = emptyList<Category>()
        val response = safeApiCall {
            foodApi.getCategories()
        }
        when (response) {
            is ApiResponse.Success -> {
                list = response.data.data
            }

            else -> {
            }
        }

        return list
    }

    private suspend fun getPopularRestaurants(): List<Restaurant> {
        var list = emptyList<Restaurant>()
        val response = safeApiCall {
            foodApi.getRestaurants(40.7128, -74.0060)
        }
        when (response) {
            is ApiResponse.Success -> {
                list = response.data.data
                _uiState.value = HomeScreenState.Success
            }

            else -> {
            }
        }

        return list
    }

    fun onRestaurantSelected(restaurant: Restaurant) {
        viewModelScope.launch {
            _navigationEvent.emit(
                HomeScreenNavigationEvents.NavigateToDetail(
                    restaurant.name,
                    restaurant.imageUrl,
                    restaurant.id
                )
            )

        }
    }

    sealed class HomeScreenState {
        data object Loading : HomeScreenState()
        data object Empty : HomeScreenState()
        data object Success : HomeScreenState()
    }

    sealed class HomeScreenNavigationEvents {
        data class NavigateToDetail(
            val name: String,
            val imageUrl: String,
            val restaurantID: String
        ) : HomeScreenNavigationEvents()
    }
}