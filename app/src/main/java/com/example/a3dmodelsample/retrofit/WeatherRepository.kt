// WeatherRepository.kt
package com.example.a3dmodelsample.retrofit

import com.example.a3dmodelsample.retrofit.data.DailyWeatherUiModel
import com.example.a3dmodelsample.retrofit.data.ForecastResponse
import com.example.a3dmodelsample.retrofit.data.FutureWeatherResponse
import com.example.a3dmodelsample.retrofit.data.HourlyWeatherUi
import com.example.a3dmodelsample.retrofit.data.WeatherResponse
import com.example.a3dmodelsample.retrofit.data.toDailyUiList
import com.example.a3dmodelsample.retrofit.data.toHourlyUi
import com.example.a3dmodelsample.retrofit.data.toHourlyUiList

class WeatherRepository(
    private val apiKey: String
) {
    private val openWeatherApi = WeatherApiClient.createOpenWeather()
    private val openMeteoArchiveApi = WeatherApiClient.createOpenMeteoArchive()

    suspend fun getWeatherByCity(cityName: String): WeatherResponse {
        return openWeatherApi.getCurrentWeatherByCity(cityName, apiKey)
    }

    suspend fun getWeatherByLocation(lat: Double, lon: Double): WeatherResponse {
        return openWeatherApi.getCurrentWeatherByLocation(lat, lon, apiKey)
    }

    suspend fun getThreeHourForecastByCity(cityName: String): List<HourlyWeatherUi> {
        val response = openWeatherApi.getThreeHourForecastByCity(cityName, apiKey)
        return response.toHourlyUiList()
    }

    suspend fun getThreeHourForecastByLocation(lat: Double, lon: Double): ForecastResponse {
        return openWeatherApi.getThreeHourForecastByLocation(lat, lon, apiKey)
    }


    suspend fun getArchiveWeather(lat: Double, lon: Double): List<DailyWeatherUiModel> {
        val response = openMeteoArchiveApi.getForecastWeather(lat, lon)
        return response.toDailyUiList()
    }


}
