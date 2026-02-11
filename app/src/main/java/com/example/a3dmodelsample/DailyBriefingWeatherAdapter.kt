package com.example.a3dmodelsample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.a3dmodelsample.retrofit.data.HourlyWeatherUi

class DailyBriefingWeatherAdapter(
    private val items: List<HourlyWeatherUi>
) : RecyclerView.Adapter<DailyBriefingWeatherAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.daily_breifing_weather_list, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, items)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvTemp: TextView = itemView.findViewById(R.id.tvTemp)
        private val ivWeather: ImageView = itemView.findViewById(R.id.ivWeather)

        fun bind(
            position: Int,
            items: List<HourlyWeatherUi>,
        ) {

            val item = items[position]

            tvTime.text = if (position == 0) {
                "NOW"
            } else {
                item.timeLabel
            }

            tvTemp.text = item.tempText
            ivWeather.setImageResource(item.iconRes)

        }
    }
}
