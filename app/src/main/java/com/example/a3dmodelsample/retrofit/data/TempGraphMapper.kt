package com.example.a3dmodelsample.retrofit.data

import android.util.Log
import com.example.a3dmodelsample.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

private val FORECAST_TIME_FORMATTER =
    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

// Response 전체 → UI 리스트
fun ForecastResponse.toHourlyUiList(): List<HourlyWeatherUi> {
    val items = list ?: return emptyList()

    return items.mapNotNull { it.toHourlyUi() }
}

// 단일 ForecastItem → 단일 HourlyWeatherUi
fun ForecastItem.toHourlyUi(): HourlyWeatherUi? {
    val mainTemp = main?.temp ?: return null   // temp 없으면 그냥 스킵

    // 1) timeLabel
    val rawTime = dt_txt
    val timeLabel = if (rawTime != null) {
        try {
            val dateTime = LocalDateTime.parse(rawTime, FORECAST_TIME_FORMATTER)
            val hour = dateTime.hour
            when {
                hour == 0 -> "12AM"
                hour < 12 -> "${hour}AM"
                hour == 12 -> "12PM"
                else -> "${hour - 12}PM"
            }
        } catch (e: Exception) {
            "--"
        }
    } else {
        "--"
    }

    // 2) tempText
    val tempInt = mainTemp.roundToInt()
    val tempText = "${tempInt}°"

    val iconCode = weather?.firstOrNull()?.main
    val iconRes = when(iconCode) {
        "Clear" -> R.drawable.weather_normalday
        "Clouds" -> R.drawable.weather_cloudyday
        "Rain" -> R.drawable.weather_rainingday
        else -> {
            Log.d("tempMapper","$iconCode")
            R.drawable.weather_snowingday
        }
    }

    return HourlyWeatherUi(
        timeLabel = timeLabel,
        tempText = tempText,
        tempValue = tempInt,
        iconRes = iconRes
    )
}