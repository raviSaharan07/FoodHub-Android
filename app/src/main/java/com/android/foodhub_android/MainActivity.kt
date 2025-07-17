package com.android.foodhub_android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.WindowInsets
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.android.foodhub_android.data.FoodApi
import com.android.foodhub_android.data.FoodHubSession
import com.android.foodhub_android.data.models.FoodItem
import com.android.foodhub_android.notification.FoodHubMessagingService
import com.android.foodhub_android.ui.features.add_address.AddAddressScreen
import com.android.foodhub_android.ui.features.address_list.AddressListScreen
import com.android.foodhub_android.ui.features.auth.AuthScreen
import com.android.foodhub_android.ui.features.auth.login.SignInScreen
import com.android.foodhub_android.ui.features.auth.signup.SignUpScreen
import com.android.foodhub_android.ui.features.cart.CartScreen
import com.android.foodhub_android.ui.features.cart.CartViewModel
import com.android.foodhub_android.ui.features.food_item_details.FoodDetailsScreen
import com.android.foodhub_android.ui.features.home.HomeScreen
import com.android.foodhub_android.ui.features.notifications.NotificationsList
import com.android.foodhub_android.ui.features.notifications.NotificationsViewModel
import com.android.foodhub_android.ui.features.order_details.OrderDetailsScreen
import com.android.foodhub_android.ui.features.order_success.OrderSuccess
import com.android.foodhub_android.ui.features.orders.OrderListScreen
import com.android.foodhub_android.ui.features.restaurant_details.RestaurantDetailsScreen
import com.android.foodhub_android.ui.navigation.AddAddress
import com.android.foodhub_android.ui.navigation.AddressList
import com.android.foodhub_android.ui.navigation.AuthScreen
import com.android.foodhub_android.ui.navigation.Cart
import com.android.foodhub_android.ui.navigation.FoodDetails
import com.android.foodhub_android.ui.navigation.Home
import com.android.foodhub_android.ui.navigation.Login
import com.android.foodhub_android.ui.navigation.NavRoute
import com.android.foodhub_android.ui.navigation.Notification
import com.android.foodhub_android.ui.navigation.OrderDetails
import com.android.foodhub_android.ui.navigation.OrderList
import com.android.foodhub_android.ui.navigation.OrderSuccess
import com.android.foodhub_android.ui.navigation.RestaurantDetails
import com.android.foodhub_android.ui.navigation.SignUp
import com.android.foodhub_android.ui.navigation.foodItemNavType
import com.android.foodhub_android.ui.theme.FoodHubAndroidTheme
import com.android.foodhub_android.ui.theme.Mustard
import com.android.foodhub_android.ui.theme.Orange
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.reflect.typeOf

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private var showSplashScreen = true

    @Inject
    lateinit var foodApi: FoodApi

    @Inject
    lateinit var session: FoodHubSession
    val viewModel by viewModels<HomeViewModel>()

    sealed class BottomNavItem(val route: NavRoute, val icon: Int) {
        data object Home :
            BottomNavItem(com.android.foodhub_android.ui.navigation.Home, R.drawable.ic_home)

        data object Cart :
            BottomNavItem(com.android.foodhub_android.ui.navigation.Cart, R.drawable.ic_cart)

        data object Notification : BottomNavItem(
            com.android.foodhub_android.ui.navigation.Notification,
            R.drawable.ic_notification
        )
        data object Orders : BottomNavItem(
            com.android.foodhub_android.ui.navigation.OrderList,
            R.drawable.ic_orders
        )
    }

    @OptIn(ExperimentalSharedTransitionApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                showSplashScreen
            }
//            setOnExitAnimationListener {screen ->
//                val zoomX = ObjectAnimator.ofFloat(
//                    screen.iconView,
//                    View.SCALE_X,
//                    0.4f,
//                    0f
//                )
//
//                val zoomY = ObjectAnimator.ofFloat(
//                    screen.iconView,
//                    View.SCALE_Y,
//                    0.4f,
//                    0f
//                )
//
//                zoomX.duration = 500
//                zoomY.duration = 500
//                zoomX.interpolator=OvershootInterpolator()
//                zoomY.interpolator=OvershootInterpolator()
//                zoomX.doOnEnd {
//                    screen.remove()
//                }
//                zoomY.doOnEnd {
//                    screen.remove()
//                }
//                zoomX.start()
//                zoomY.start()
//
//            }
        }
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FoodHubAndroidTheme {
                val shouldShowBottomNav = remember {
                    mutableStateOf(false)
                }
                val cartViewModel: CartViewModel = hiltViewModel()
                val notificationViewModel : NotificationsViewModel = hiltViewModel()

                val cartItemSize = cartViewModel.cartItemCount.collectAsStateWithLifecycle()
                val unreadCount = notificationViewModel.unreadCount.collectAsStateWithLifecycle()

                val navController = rememberNavController()

                LaunchedEffect(key1 = true) {
                    viewModel.event.collectLatest{
                        when(it){
                            is HomeViewModel.HomeEvent.NavigateToOrderDetail -> {
                                navController.navigate(OrderDetails(it.orderID))
                            }
                        }
                    }
                }

                val navItems = listOf(
                    BottomNavItem.Home,
                    BottomNavItem.Cart,
                    BottomNavItem.Notification,
                    BottomNavItem.Orders
                )
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        val currentRoute =
                            navController.currentBackStackEntryAsState().value?.destination
                        AnimatedVisibility(visible = shouldShowBottomNav.value) {
                            NavigationBar(
                                containerColor = Color.White
                            ) {
                                navItems.forEach { item ->
                                    val selected =
                                        currentRoute?.hierarchy?.any { it.route == item.route::class.qualifiedName } == true

                                    NavigationBarItem(
                                        selected = selected,
                                        onClick = {
                                            navController.navigate(item.route)
                                        },
                                        icon = {
                                            Box(modifier = Modifier.size(48.dp)) {
                                                Icon(
                                                    painter = painterResource(id = item.icon),
                                                    contentDescription = null,
                                                    tint = if (selected) Orange else Color.Gray,
                                                    modifier = Modifier.align(Alignment.Center)
                                                )
                                                if (item.route == Cart && cartItemSize.value > 0) {
                                                    ItemCount(cartItemSize.value)
                                                }
                                                if(item.route == Notification && unreadCount.value > 0){
                                                    ItemCount(unreadCount.value)
                                                }
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }) { innerPadding ->
                    SharedTransitionLayout {
                        NavHost(
                            navController = navController,
                            startDestination = if (session.getToken() != null) Home else AuthScreen,
                            modifier = Modifier.padding(innerPadding),
//                            enterTransition = {
//                                slideIntoContainer(
//                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
//                                    animationSpec = tween(300)
//                                ) + fadeIn(animationSpec = tween(300))
//                            },
//                            exitTransition = {
//                                slideOutOfContainer(
//                                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
//                                    animationSpec = tween(300)
//                                ) + fadeOut(animationSpec = tween(300))
//                            },
//                            popEnterTransition = {
//                                slideIntoContainer(
//                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
//                                    animationSpec = tween(300)
//                                ) + fadeIn(animationSpec = tween(300))
//                            },
//                            popExitTransition = {
//                                slideOutOfContainer(
//                                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
//                                    animationSpec = tween(300)
//                                ) + fadeOut(animationSpec = tween(300))
//                            }
                        ) {
                            composable<SignUp> {
                                shouldShowBottomNav.value = false
                                SignUpScreen(navController)
                            }
                            composable<AuthScreen> {
                                shouldShowBottomNav.value = false
                                AuthScreen(navController)
                            }
                            composable<Login> {
                                shouldShowBottomNav.value = false
                                SignInScreen(navController)
                            }
                            composable<Home> {
                                shouldShowBottomNav.value = true
                                HomeScreen(navController, this)
                            }
                            composable<RestaurantDetails> {
                                shouldShowBottomNav.value = false
                                val route = it.toRoute<RestaurantDetails>()
                                RestaurantDetailsScreen(
                                    navController,
                                    name = route.restaurantName,
                                    imageUrl = route.restaurantImageUrl,
                                    animatedVisibilityScope = this,
                                    restaurantID = route.restaurantID
                                )
                            }
                            composable<FoodDetails>(
                                typeMap = mapOf(typeOf<FoodItem>() to foodItemNavType)
                            ) {
                                shouldShowBottomNav.value = false
                                val route = it.toRoute<FoodDetails>()
                                FoodDetailsScreen(
                                    navController,
                                    foodItem = route.foodItem,
                                    this,
                                    onItemAddedToCart = {
                                        cartViewModel.getCart()
                                    }
                                )
                            }
                            composable<Cart> {
                                shouldShowBottomNav.value = true
                                CartScreen(navController, cartViewModel)
                            }
                            composable<Notification> {
                                SideEffect {
                                    shouldShowBottomNav.value = true
                                }
                                NotificationsList(navController,notificationViewModel)
                            }
                            composable<AddressList> {
                                shouldShowBottomNav.value = false
                                AddressListScreen(navController)
                            }
                            composable<AddAddress>{
                                shouldShowBottomNav.value = false
                                AddAddressScreen(navController)
                            }
                            composable<OrderSuccess>{
                                shouldShowBottomNav.value = false
                                val orderID = it.toRoute<OrderSuccess>().orderId
                                OrderSuccess(orderID,navController)
                            }
                            composable<OrderList>{
                                shouldShowBottomNav.value = true
                                OrderListScreen(navController)
                            }
                            composable<OrderDetails>{
                                SideEffect {
                                    shouldShowBottomNav.value = false
                                }
                                val orderID = it.toRoute<OrderDetails>().orderId
                                OrderDetailsScreen(navController,orderID)
                            }
                        }
                    }
                }
            }
        }

        if (::foodApi.isInitialized) {
            Log.d("MainActivity", "FoodApi is initialized")
        }

        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            showSplashScreen = false
            processIntent(intent,viewModel)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        processIntent(intent,viewModel)
    }

    private fun processIntent(intent: Intent, viewModel: HomeViewModel){
        if(intent.hasExtra(FoodHubMessagingService.ORDER_ID)){
            val orderID = intent.getStringExtra(FoodHubMessagingService.ORDER_ID)
            viewModel.navigateToOrderDetail(orderID!!)
            intent.removeExtra(FoodHubMessagingService.ORDER_ID)
        }
    }
}

@Composable
fun BoxScope.ItemCount(count: Int){
    Box(
        modifier = Modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(Mustard)
            .align(Alignment.TopEnd)
    ) {
        Text(
            text = "$count",
            modifier = Modifier
                .align(Alignment.Center),
            color = Color.White,
            style = TextStyle(fontSize = 10.sp)
        )
    }
}