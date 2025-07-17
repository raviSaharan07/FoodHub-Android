package com.android.foodhub_android.ui.features.orders

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
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
import com.android.foodhub_android.data.models.Order
import com.android.foodhub_android.ui.navigation.OrderDetails
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun OrderListScreen(navController: NavController, viewModel: OrderListViewModel = hiltViewModel()) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val uiState = viewModel.uiState.collectAsStateWithLifecycle()

        LaunchedEffect(key1 = true){
            viewModel.event.collectLatest {
                when(it){
                    is OrderListViewModel.OrderListEvent.NavigateToOrderDetailScreen -> {
                           navController.navigate(OrderDetails( it.order.id))
                    }
                    is OrderListViewModel.OrderListEvent.NavigateBack -> {
                        navController.popBackStack()
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_back_button),
                contentDescription = "Back Button",
                modifier = Modifier
                    .shadow(12.dp,clip = true)
                    .clip(CircleShape)
                    .clickable { viewModel.navigationBack() }
            )
            Text(
                text = "Orders",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.size(48.dp))
        }

        when (uiState.value) {
            is OrderListViewModel.OrderListState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Text(text = "Loading")
                }
            }

            is OrderListViewModel.OrderListState.OrderList -> {
                val list = (uiState.value as OrderListViewModel.OrderListState.OrderList).orderList
                if (list.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "No orders found", textAlign = TextAlign.Center)
                    }
                } else {
                    val listOfTabs = listOf("Upcoming", "History")
                    val coroutineScope = rememberCoroutineScope()
                    val pagerState = rememberPagerState(
                        pageCount = { listOfTabs.size },
                        initialPage = 0
                    )
                    TabRow(
                        selectedTabIndex = pagerState.currentPage,
                        modifier = Modifier
                            .padding(horizontal = 16.dp,vertical = 2.dp)
                            .clip(RoundedCornerShape(32.dp))
                            .border(
                                width = 1.dp,
                                color = Color.LightGray,
                                shape = RoundedCornerShape(32.dp)
                            ),
                        indicator = {},
                        divider = {}
                    ) {
                        listOfTabs.forEachIndexed { index, title ->
                            Tab(
                                text = {
                                    Text(
                                        text = title,
                                        color = if (pagerState.currentPage == index) Color.White else Color.Gray
                                    )
                                },
                                selected = pagerState.currentPage == index,
                                onClick = {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(index)
                                    }
                                },
                                modifier = Modifier
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(32.dp))
                                    .background(if (pagerState.currentPage == index) MaterialTheme.colorScheme.primary else Color.White)
                            )
                        }
                    }
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) {
                        when (it) {
                            0 -> {
                                OrderListInternal(
                                    list.filter { order -> order.status == "PENDING" || order.status == "PENDING_ACCEPTANCE" },
                                    onClick = { order ->
                                        viewModel.navigationToDetails(order)
                                    }
                                )
                            }

                            1 -> {
                                OrderListInternal(
                                    list.filter { order -> order.status != "PENDING" && order.status != "PENDING_ACCEPTANCE" },
                                    onClick = { order ->
                                        viewModel.navigationToDetails(order)
                                    }
                                )
                            }

                        }
                    }
                }
            }

            is OrderListViewModel.OrderListState.Error -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = (uiState.value as OrderListViewModel.OrderListState.Error).message)
                    Button(onClick = { viewModel.getOrders() }) {
                        Text(text = "Retry")
                    }
                }
            }

        }
    }
}

@Composable
fun OrderDetailsText(order: Order) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = order.restaurant.imageUrl,
                contentDescription = "Restaurant Image",
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(
                    text = order.id,
//                    textAlign = TextAlign.End,
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1
                )
                Text(
                    text = "${order.items.size} items",
                    color = Color.Gray
                )
                Text(
                    text = order.restaurant.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black
                )
            }
        }
        Text(text = "Status", color = Color.Gray)
        Text(text = order.status, color = Color.Black)
        Spacer(modifier = Modifier.size(4.dp))
    }
}

@Composable
fun OrderListInternal(list: List<Order>, onClick: (Order) -> Unit) {
    if (list.isEmpty()) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = "No orders found"
            )
        }
    } else {
        LazyColumn {
            items(list) { order ->
                OrderListItem(order = order, onClick = { onClick(order) })
            }
        }
    }
}

@Composable
fun OrderListItem(order: Order, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .fillMaxWidth()
            .shadow(8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(color = Color.White)
            .padding(16.dp)
    ) {
        OrderDetailsText(order)
        Button(onClick = onClick) {
            Text(
                text = "View Details",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        }
    }
}
