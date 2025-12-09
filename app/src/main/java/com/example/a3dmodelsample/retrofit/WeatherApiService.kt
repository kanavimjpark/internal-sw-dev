// WeatherApiService.kt
package com.example.a3dmodelsample.retrofit

import com.example.a3dmodelsample.retrofit.data.ForecastResponse
import com.example.a3dmodelsample.retrofit.data.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    // Get current weather by city name
    @GET("weather")
    suspend fun getCurrentWeatherByCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",   // metric = Celsius
        @Query("lang") lang: String = "kr"          // Korean descriptions
    ): WeatherResponse

    // Get current weather by latitude/longitude
    @GET("weather")
    suspend fun getCurrentWeatherByLocation(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "kr"
    ): WeatherResponse

    // ⬇️ 3-hour step forecast by city name (5 days / 3h)
    @GET("forecast")
    suspend fun getThreeHourForecastByCity(
        @Query("q") cityName: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "kr"
    ): ForecastResponse

    // ⬇️ 3-hour step forecast by coordinates
    @GET("forecast")
    suspend fun getThreeHourForecastByLocation(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "kr"
    ): ForecastResponse

}
