// WeatherApiClient.kt
package com.example.a3dmodelsample.retrofit

import com.example.a3dmodelsample.OpenMeteoApiService

object WeatherApiClient {

    private const val BASE_URL_OPENWEATHER = "https://api.openweathermap.org/data/2.5/"

    private const val BASE_URL_OPEN_METEO_ARCHIVE =
        "https://api.open-meteo.com/v1/"

    fun createOpenWeather(): WeatherApiService {
        return RetrofitFactory.createService(
            baseUrl = BASE_URL_OPENWEATHER
        ) {}
    }

    fun createOpenMeteoArchive(): OpenMeteoApiService {
        return RetrofitFactory.createService(
            baseUrl = BASE_URL_OPEN_METEO_ARCHIVE
        ) {}
    }
}
