package com.example.a3dmodelsample

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import coil.dispose
import coil.load
import com.example.a3dmodelsample.retrofit.data.BundleProgram

class DetailAdapter(private val detailList : List<BundleProgram>,
                    private val onItemClick: (String) -> Unit,)
    : RecyclerView.Adapter<DetailAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val rvDetailListImage: ImageView = itemView.findViewById(R.id.videoThumbnail)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount() = detailList.size


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val program = detailList[position]

        val imageUrl = program.images?.firstOrNull {it.orientation == "LANDSCAPE"}?.URL

        holder.rvDetailListImage.load(imageUrl)
        holder.rvDetailListImage.setOnClickListener {
            Log.d("mjpark","ProgramID : ${program.programID}")
            val programID = program.programID
            onItemClick(programID)
        }
    }

    override fun onViewRecycled(holder: ViewHolder) {
        holder.rvDetailListImage.dispose()
        holder.rvDetailListImage.setImageDrawable(null)
        super.onViewRecycled(holder)
    }
}