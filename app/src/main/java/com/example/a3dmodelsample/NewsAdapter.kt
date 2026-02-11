package com.example.a3dmodelsample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.a3dmodelsample.retrofit.data.Article
import coil.load
import coil.size.Scale

class NewsAdapter(
    private val items: List<Article>
) : RecyclerView.Adapter<NewsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.daily_breifing_news_list, parent, false)

        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val tvNewsChannel: TextView = itemView.findViewById(R.id.tvNewsChannel)
        private val tvNewsTitle: TextView = itemView.findViewById(R.id.tvNewsTitle)
        private val ivNewsThumbnail: ImageView = itemView.findViewById(R.id.ivNewsThumbnail)

        fun bind(
            article: Article
        ) {
            ivNewsThumbnail.load(article.urlToImage) {
                crossfade(true)
                placeholder(R.drawable.radius24)
                error(R.drawable.radius24)

                size(162, 162)
                scale(Scale.FILL)
            }

            tvNewsChannel.text = article.source?.name ?: "channel"
            tvNewsTitle.text = article.title ?: ""


        }
    }
}
