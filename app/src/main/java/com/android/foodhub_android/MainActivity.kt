package com.android.foodhub_android

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.android.foodhub_android.data.FoodApi
import com.android.foodhub_android.data.FoodHubSession
import com.android.foodhub_android.data.models.FoodItem
import com.android.foodhub_android.ui.features.auth.AuthScreen
import com.android.foodhub_android.ui.features.auth.login.SignInScreen
import com.android.foodhub_android.ui.features.auth.signup.SignUpScreen
import com.android.foodhub_android.ui.features.food_item_details.FoodDetailsScreen
import com.android.foodhub_android.ui.features.home.HomeScreen
import com.android.foodhub_android.ui.features.restaurant_details.RestaurantDetailsScreen
import com.android.foodhub_android.ui.navigation.AuthScreen
import com.android.foodhub_android.ui.navigation.FoodDetails
import com.android.foodhub_android.ui.navigation.Home
import com.android.foodhub_android.ui.navigation.Login
import com.android.foodhub_android.ui.navigation.RestaurantDetails
import com.android.foodhub_android.ui.navigation.SignUp
import com.android.foodhub_android.ui.navigation.foodItemNavType
import com.android.foodhub_android.ui.theme.FoodHubAndroidTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                   val navController = rememberNavController()

                    SharedTransitionLayout {
                        NavHost(navController = navController,
                            startDestination = if(session.getToken()!= null) Home else AuthScreen,
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
                        ){
                            composable<SignUp>{
                                SignUpScreen(navController)
                            }
                            composable<AuthScreen>{
                                AuthScreen(navController)
                            }
                            composable<Login>{
                                SignInScreen(navController)
                            }
                            composable<Home>{
                                HomeScreen(navController,this)
                            }
                            composable<RestaurantDetails>{
                                val route = it.toRoute<RestaurantDetails>()
                                RestaurantDetailsScreen(
                                    navController,
                                    name = route.restaurantName,
                                    imageUrl = route.restaurantImageUrl,
                                    animatedVisibilityScope = this,
                                    restaurantID =  route.restaurantID)
                            }
                            composable<FoodDetails>(
                                typeMap = mapOf(typeOf<FoodItem>() to foodItemNavType)
                            ){
                                val route = it.toRoute<FoodDetails>()
                                FoodDetailsScreen(
                                    navController,
                                    foodItem = route.foodItem,
                                    this
                                )
                            }
                        }
                    }
                }
            }
        }

        if(::foodApi.isInitialized){
            Log.d("MainActivity","FoodApi is initialized")
        }

        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)
            showSplashScreen = false
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    FoodHubAndroidTheme {
//
//    }
//}