package com.example.a3dmodelsample.retrofit.data

data class FutureWeatherResponse(
    val latitude: Double,
    val longitude: Double,
    val generationtime_ms: Double?,
    val daily_units: DailyUnits?,
    val daily: DailyData?
)

data class DailyUnits(
    val time: String?,
    val weathercode: String?,
    val temperature_2m_max: String?,
    val temperature_2m_min: String?
)

data class DailyData(
    val time: List<String>?,
    val weathercode: List<Int>?,
    val temperature_2m_max: List<Double>?,
    val temperature_2m_min: List<Double>?
)


data class DailyWeatherUiModel(
    val date: String,          // "2025-12-05"
    val minTemp: Double,
    val maxTemp: Double,
    val weatherCode: Int?      // 아이콘 결정용
)
