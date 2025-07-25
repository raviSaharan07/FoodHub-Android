// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.dagger.hilt) apply false//DaggerHilt
    alias(libs.plugins.devtools.ksp) apply false//ksp
    alias(libs.plugins.kotlin.serialization) apply false// Kotlin Serialization
    alias(libs.plugins.kotlin.parcelize) apply false //Kotlin Parcelize
    alias(libs.plugins.google.services) apply false // Google Services for Firebase
}