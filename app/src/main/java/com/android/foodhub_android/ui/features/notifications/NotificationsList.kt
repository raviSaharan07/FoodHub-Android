package com.android.foodhub_android.ui.features.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.android.foodhub_android.data.models.Notification
import com.android.foodhub_android.ui.navigation.OrderDetails
import kotlinx.coroutines.flow.collectLatest

@Composable
fun NotificationsList(
    navController: NavController,
    viewModel: NotificationsViewModel
) {
    val state = viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(key1 = true) {
        viewModel.event.collectLatest {
            when (it) {
                is NotificationsViewModel.NotificationsEvent.NavigateToOrderDetails -> {
                    navController.navigate(OrderDetails(it.orderID))
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        when (state.value) {
            is NotificationsViewModel.NotificationsState.Loading -> {
                LoadingScreen()
            }

            is NotificationsViewModel.NotificationsState.Success -> {
                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                )
                val notifications =
                    (state.value as NotificationsViewModel.NotificationsState.Success).notifications
                LazyColumn {
                    items(notifications, key = { it.id }) { notification ->
                        NotificationItem(notification = notification) {
                            viewModel.readNotification(notification)
                        }
                    }
                }
            }

            is NotificationsViewModel.NotificationsState.Error -> {
                ErrorScreen(message = (state.value as NotificationsViewModel.NotificationsState.Error).message) {
                    viewModel.getNotifications()
                }
            }
        }
    }
}

@Composable
fun NotificationItem(notification: Notification, onRead: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (notification.isRead) Color.Transparent else MaterialTheme.colorScheme.primary.copy(
                    alpha = 0.1f
                )
            )
            .clickable { onRead() }
            .padding(16.dp)
    ) {
        Text(text = notification.title, style = MaterialTheme.typography.titleMedium)
        Text(text = notification.message, style = MaterialTheme.typography.bodySmall)
    }
}


@Composable
fun LoadingScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorScreen(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message, style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onRetry) {
            Text(text = "Retry")
        }

    }
}