// ForecastResponse.kt
package com.example.a3dmodelsample.retrofit.data

data class ForecastResponse(
    val cod: String?,
    val message: Double?,
    val cnt: Int?,
    val list: List<ForecastItem>?,
    val city: ForecastCity?
)

data class ForecastItem(
    val dt: Long?,
    val dt_txt: String?,
    val main: ForecastMain?,
    val weather: List<ForecastWeather>?,
    val wind: ForecastWind?
    // add more fields if needed
)

data class ForecastMain(
    val temp: Double?,
    val feels_like: Double?,
    val temp_min: Double?,
    val temp_max: Double?,
    val humidity: Int?
)

data class ForecastWeather(
    val main: String?,
    val description: String?,
    val icon: String?
)

data class ForecastWind(
    val speed: Double?,
    val deg: Int?
)

data class ForecastCity(
    val name: String?,
    val country: String?
)


data class HourlyWeatherUi(
    val timeLabel: String,    // e.g. "NOW", "9AM"
    val tempText: String,
    val tempValue: Int,// e.g. "5°"
    val iconRes: Int          // e.g. R.drawable.ic_sun_day
)
