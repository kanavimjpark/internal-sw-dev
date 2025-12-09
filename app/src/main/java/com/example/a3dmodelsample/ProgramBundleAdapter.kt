package com.example.a3dmodelsample

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.net.toUri
import androidx.recyclerview.widget.RecyclerView
import coil.dispose
import coil.load
import com.example.a3dmodelsample.retrofit.data.BundleProgram

class ProgramBundleAdapter(
    private val programList: List<BundleProgram>,
    private val genreType: String,
    private val onItemClick: (String) -> Unit
) :
    RecyclerView.Adapter<ProgramBundleAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProgramImage: ImageView = itemView.findViewById(R.id.videoThumbnail)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video, parent, false)
        return ViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val program = programList[position]
        Log.d("mjpark","@@@@@@@@@@@@@@@@@ : ${program.programID.toUri()}")
        val imageUrl = program.images?.firstOrNull {it.orientation == "LANDSCAPE"}?.URL

        holder.ivProgramImage.load(imageUrl)
        holder.ivProgramImage.setOnClickListener {
            Log.d("mjpark","ProgramID : ${program.programID}")
            val programID = program.programID
            onItemClick(programID)
        }
    }

    override fun getItemCount(): Int = programList.size

    override fun onViewRecycled(holder: ViewHolder) {
        holder.ivProgramImage.dispose()
        holder.ivProgramImage.setImageDrawable(null)
        super.onViewRecycled(holder)
    }
}