package com.example.a3dmodelsample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.a3dmodelsample.retrofit.data.HourlyWeatherUi

class TempGraphAdapter(
    private val items: List<HourlyWeatherUi>
) : RecyclerView.Adapter<TempGraphAdapter.ViewHolder>() {

    // Correct min/max temp calculation
    private val minTemp = items.minOf { it.tempValue }
    private val maxTemp = items.maxOf { it.tempValue }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_temp_graph, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position, items, minTemp, maxTemp)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        private val tvTemp: TextView = itemView.findViewById(R.id.tvTemp)
        private val ivWeather: ImageView = itemView.findViewById(R.id.ivWeather)

        private val graphArea: View = itemView.findViewById(R.id.graphArea)
        private val groupIconTemp: View = itemView.findViewById(R.id.groupIconTemp)
        private val viewBar: View = itemView.findViewById(R.id.viewBar)

        fun bind(
            position: Int,
            items: List<HourlyWeatherUi>,
            minTemp: Int,
            maxTemp: Int
        ) {

            val item = items[position]

            tvTime.text = if (position == 0) {
                "NOW"
            } else {
                item.timeLabel
            }

            tvTemp.text = item.tempText
            ivWeather.setImageResource(item.iconRes)

            // ========== Core: temperature-based Y translation ==========
            graphArea.post {
                val areaHeight = graphArea.height
                val groupHeight = groupIconTemp.height

                val ratio = if (maxTemp == minTemp) 0.5f
                else (item.tempValue - minTemp).toFloat() / (maxTemp - minTemp)

                val minY = 0f
                val maxY = (areaHeight - groupHeight).toFloat()

                val y = maxY - (maxY - minY) * ratio // higher temp = upper position

                groupIconTemp.translationY = y

                viewBar.translationY = y
            }

            // ========== Optional: bar angle ==========
            val nextTemp = items.getOrNull(position + 1)?.tempValue
            if (nextTemp == null) {
                viewBar.visibility = View.GONE
            } else {
                viewBar.visibility = View.VISIBLE
            }

            viewBar.rotation = when {
                nextTemp == null -> 0f
                nextTemp > item.tempValue -> -12f
                nextTemp < item.tempValue -> 12f
                else -> 0f
            }
        }
    }
}
