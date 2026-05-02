package com.example.hello.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object WeatherRepository {
    private const val BASE_URL = "https://api.open-meteo.com/"

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val weatherApi = retrofit.create(WeatherApi::class.java)

    suspend fun getWeather(city: City): Result<WeatherResponse> {
        return try {
            val response = weatherApi.getWeather(city.latitude, city.longitude)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}