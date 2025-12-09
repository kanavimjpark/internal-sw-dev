package com.example.a3dmodelsample

import com.example.a3dmodelsample.retrofit.data.FutureWeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenMeteoApiService {

    @GET("forecast")
    suspend fun getForecastWeather(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("daily") daily: String =
            "weathercode,temperature_2m_max,temperature_2m_min",
        @Query("timezone") timezone: String = "Asia/Seoul"
    ): FutureWeatherResponse

}
