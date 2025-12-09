package com.example.a3dmodelsample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.a3dmodelsample.retrofit.data.DailyWeatherUiModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class WeatherAdapter(
    private val itemList: List<DailyWeatherUiModel>
) : RecyclerView.Adapter<WeatherAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val date: TextView = itemView.findViewById(R.id.tv_day)
        val ivWeather: ImageView = itemView.findViewById(R.id.iv_weather)
        val tvMinDegree: TextView = itemView.findViewById(R.id.tv_min_degree)
        val tvMaxDegree: TextView = itemView.findViewById(R.id.tv_max_degree)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_weather_forecast, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]

        // 날짜: "2025-12-05" → "금" 이런 식으로 혹은 그냥 날짜 표시

        holder.date.text = formatDateToEnglishDay(item.date)


        val iconRes = when (item.weatherCode) {
            0 -> R.drawable.weather_normalday                                   // 맑음
            1, 2 -> R.drawable.weather_normalday        // 거의 맑음 / 부분 흐림
            3 -> R.drawable.weather_cloudyday           // 흐림 (있다면)
            in 51..67 -> R.drawable.weather_rainingday  // 이슬비/비
            in 71..77 -> R.drawable.weather_snowingday  // 눈
            in 80..82 -> R.drawable.weather_rainingday  // 소나기
            in 95..99 -> R.drawable.weather_thunderstromday // 뇌우
            else -> R.drawable.weather_normalday        // 기본값
        }
        holder.ivWeather.setImageResource(iconRes)

        // 온도 (소수점 정리)
        holder.tvMinDegree.text = String.format("%.0f", item.minTemp)
        holder.tvMaxDegree.text = String.format("%.0f", item.maxTemp)
    }

    override fun getItemCount(): Int = itemList.size

    private fun formatDateToEnglishDay(dateStr: String): String {
        return try {
            val localDate = LocalDate.parse(dateStr, DateTimeFormatter.ISO_DATE)
            val today = LocalDate.now()

            if (localDate.isEqual(today)) {
                "Today"
            } else {
                localDate.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
            }
        } catch (e: Exception) {
            dateStr
        }
    }

}
