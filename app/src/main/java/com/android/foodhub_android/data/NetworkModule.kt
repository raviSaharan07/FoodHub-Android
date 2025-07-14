package com.android.foodhub_android.data

import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    fun provideClient(session: FoodHubSession): OkHttpClient{
        val client = OkHttpClient.Builder()
        client.addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("Authorization", "Bearer ${session.getToken()}")
                .build()
            chain.proceed(request)
        }
        client.addInterceptor(HttpLoggingInterceptor().apply{
            level = HttpLoggingInterceptor.Level.BODY
            })

        return client.build()
    }

    @Provides
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .client(client)
            .baseUrl("http://192.168.0.6:8080")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    }

    @Provides
    fun provideFoodApi(retrofit: Retrofit): FoodApi {
        return retrofit.create(FoodApi::class.java)
    }

    @Provides
    fun provideSession(@ApplicationContext context: Context): FoodHubSession{
        return FoodHubSession(context)
    }

    @Provides
    fun provideLocationService(@ApplicationContext context: Context): FusedLocationProviderClient{
        return LocationServices.getFusedLocationProviderClient(context)
    }

}