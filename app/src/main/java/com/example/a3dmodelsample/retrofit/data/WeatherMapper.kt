package com.example.a3dmodelsample.retrofit.data

fun FutureWeatherResponse.toDailyUiList(): List<DailyWeatherUiModel> {
    val dailyData = daily ?: return emptyList()

    val times = dailyData.time ?: return emptyList()
    val mins = dailyData.temperature_2m_min ?: emptyList()
    val maxs = dailyData.temperature_2m_max ?: emptyList()
    val codes = dailyData.weathercode ?: emptyList()

    val size = times.size
    val list = mutableListOf<DailyWeatherUiModel>()

    for (i in 0 until size) {
        val date = times[i]
        val min = mins.getOrNull(i) ?: 0.0
        val max = maxs.getOrNull(i) ?: 0.0
        val code = codes.getOrNull(i)

        list.add(
            DailyWeatherUiModel(
                date = date,
                minTemp = min,
                maxTemp = max,
                weatherCode = code
            )
        )
    }
    return list
}