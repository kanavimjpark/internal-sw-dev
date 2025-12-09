package com.example.a3dmodelsample.retrofit.data

data class WeatherResponse(
    val weather: List<WeatherDescription>?,
    val main: WeatherMain?,
    val wind: WeatherWind?,
    val name: String?               // city name
)

data class WeatherDescription(
    val main: String?,              // e.g. "Clouds"
    val description: String?,       // e.g. "overcast clouds"
    val icon: String?               // icon id (e.g. "04d")
)

data class WeatherMain(
    val temp: Float?,               // current temp
    val feels_like: Float?,         // feels like temp
    val temp_min: Float?,
    val temp_max: Float?,
    val humidity: Int?,
    val pressure: Int?
)

data class WeatherWind(
    val speed: Float?,              // wind speed (m/s)
    val deg: Int?                   // wind direction
)

