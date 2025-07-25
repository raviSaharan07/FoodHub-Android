import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.dagger.hilt)//DaggerHilt
    alias(libs.plugins.devtools.ksp)//ksp
    alias(libs.plugins.kotlin.serialization)//Kotlin Serialization
    alias(libs.plugins.kotlin.parcelize)//Kotlin Parcelize
    alias(libs.plugins.google.services) // Google Services
}

//Extracting the keys defined in local.properties
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        load(FileInputStream(file))
    }
}
val mapsApiKey = localProperties["mapsApiKey"] as? String ?: ""
val facebookAppId = localProperties["facebookAppId"] as? String ?: ""
val facebookClientToken = localProperties["facebookClientToken"] as? String ?: ""

android {
    namespace = "com.android.foodhub_android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.android.foodhub_android_ravi"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey
        manifestPlaceholders["FACEBOOK_APP_ID"] = facebookAppId
        manifestPlaceholders["FACEBOOK_CLIENT_ID"] = facebookClientToken
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.core.splashScreen) //SplashScreen
    implementation(libs.kotlinx.coroutines.android) //Coroutine Android
    implementation(libs.dagger.hilt)//DaggerHilt
    implementation(libs.google.googleid)
    ksp(libs.dagger.hilt.compiler)//DaggerHiltCompiler
    implementation(libs.retrofit)//Retrofit
    implementation(libs.converter.gson)//Converter Gson
    implementation(libs.logging.interceptor)//Logging Interceptor
    implementation(libs.androidx.hilt.navigation.compose) //To use Hilt with Compose Navigation
    implementation(libs.androidx.lifecycle.runtime.compose) // Connects Jetpack Compose with Android Lifecycle system
    implementation(libs.androidx.navigation.compose)// Navigation Compose
    implementation(libs.kotlinx.serialization.json)// Kotlinx Serialization
    implementation(libs.androidx.credentials) // Password less authentication across different identity providers
    implementation(libs.androidx.credentials.play.services.auth) // Bridge between credentials and Google play services
    implementation(libs.google.googleid)// Google one tap sign in
    implementation(libs.facebook.android.sdk)// Facebook Sign In SDK
    implementation(libs.coil.compose) // Coil Image Loader
    implementation(libs.coil.network.okhttp) // Coil Image Loader
    implementation(libs.androidx.foundation)// Foundation for Jetpack Compose
    implementation(libs.androidx.animation) // Animation for Jetpack Compose
    implementation(libs.maps.compose) // Maps for Jetpack Compose
    implementation(libs.play.services.location) // Location for Google Play Services
    implementation(libs.kotlinx.coroutines.play.services) // Coroutines for Google Play Services
    implementation(libs.stripe.android) // Stripe for Android
    implementation(platform(libs.firebase.bom)) // Firebase BOM
    implementation(libs.firebase.messaging) // Firebase Messaging

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}