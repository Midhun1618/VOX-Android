package com.voxcom.vox.data.repository

import com.voxcom.vox.BuildConfig
import com.voxcom.vox.network.WeatherApi
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherRepository {

    private val api: WeatherApi = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(WeatherApi::class.java)

    suspend fun getWeather(lat: Double, lon: Double): String? {
        val response = api.getWeatherByLatLon(lat, lon, BuildConfig.WEATHER_API_KEY)
        return response.body()?.main?.temp?.let { "$it°C" }
    }
}
