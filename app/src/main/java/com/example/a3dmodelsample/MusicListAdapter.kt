package com.example.a3dmodelsample

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView


class MusicListAdapter(private val itemList: List<MusicItem>) :
    RecyclerView.Adapter<MusicListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        var listNum: TextView = itemView.findViewById(R.id.tv_list_num)
        var ivAlbum: ImageView = itemView.findViewById(R.id.iv_song_image)
        var tvTitle: TextView = itemView.findViewById(R.id.tv_song_title)
        var tvArtist: TextView = itemView.findViewById(R.id.tv_artist_name)
        var tvAlbumName: TextView = itemView.findViewById(R.id.tv_album)
        var tvTime: TextView = itemView.findViewById(R.id.tv_song_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.music_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]
        holder.listNum.text = item.index.toString()
        holder.ivAlbum.setImageResource(item.albumArt)
        holder.tvTitle.text = item.title
        holder.tvArtist.text = item.artist
        holder.tvAlbumName.text = item.album
        holder.tvTime.text = formatDuration(item.duration)
    }

    override fun getItemCount(): Int = itemList.size

    private fun formatDuration(ms: Long): String {
        val minutes = ms / 60000
        val seconds = (ms % 60000) / 1000
        return String.format("%d:%02d", minutes, seconds)
    }
}
