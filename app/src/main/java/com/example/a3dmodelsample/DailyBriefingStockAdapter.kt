package com.example.a3dmodelsample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.a3dmodelsample.retrofit.data.EtfQuoteUi

class DailyBriefingStockAdapter(
    private val items: List<EtfQuoteUi>
) : RecyclerView.Adapter<DailyBriefingStockAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.daily_breifing_stock_list, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvStockName: TextView = itemView.findViewById(R.id.tv_stock_name)
        private val tvCurrentPrice: TextView = itemView.findViewById(R.id.tv_current_price)
        private val tvPercentage: TextView = itemView.findViewById(R.id.tv_percentage)

        fun bind(
            etf: EtfQuoteUi
        ) {
            tvStockName.text = etf.displayName
            tvCurrentPrice.text = etf.currentPrice.toString()
            tvPercentage.text = etf.change.toString()
        }
    }
}
