package com.example.a3dmodelsample

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import android.view.View
import com.example.a3dmodelsample.retrofit.data.BundleProgram
import java.net.URL
import android.graphics.BitmapFactory
import android.widget.ImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProgramAdapter(private val itemList: List<BundleProgram>) :
    RecyclerView.Adapter<ProgramAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProgramImage: ImageView = itemView.findViewById(R.id.videoThumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = itemList[position]

        val imageUrl = item.images?.firstOrNull()?.URL
        if (!imageUrl.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val inputStream = URL(imageUrl).openStream()
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    CoroutineScope(Dispatchers.Main).launch {
                        holder.ivProgramImage.setImageBitmap(bitmap)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    CoroutineScope(Dispatchers.Main).launch {
                        holder.ivProgramImage.setImageDrawable(null)
                    }
                }
            }
        } else {
            holder.ivProgramImage.setImageDrawable(null)
        }
    }

    override fun getItemCount(): Int = itemList.size
}