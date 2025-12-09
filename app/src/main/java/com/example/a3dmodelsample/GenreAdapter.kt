package com.example.a3dmodelsample

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a3dmodelsample.retrofit.data.BundleProgram
import com.example.a3dmodelsample.retrofit.data.ProgramBundle

class GenreAdapter(
    private val genreList: List<ProgramBundle>,
    private val genreType: String,
    private val onItemClick: (String) -> Unit, // 클릭 콜백
    private val onArrowClick: (String) -> Unit
) : RecyclerView.Adapter<GenreAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvGenre: TextView = itemView.findViewById(R.id.tv_genre)
        val ivArrow: ImageView = itemView.findViewById(R.id.iv_arrow)
        val rvMovieList: RecyclerView = itemView.findViewById(R.id.videoRecyclerView)
           }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.video_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val bundleToProgramNames: Map<String, List<BundleProgram>> =
            genreList.associate { bundle ->
                val bundleName = bundle.name ?: "(no bundle name)"
                Log.d("mjpark", "bundleName : $bundleName ")

                val topPrograms = when (genreType) {
                    "Disney+" -> bundle.programs.filter { it.availableOn?.firstOrNull()?.catalog?.name.equals("Disney+ US") }.take(4)
                    "Netflix" -> bundle.programs.filter { it.availableOn?.firstOrNull()?.catalog?.name.equals("Netflix") }.take(4)
                    "HULU" -> bundle.programs.filter { it.availableOn?.firstOrNull()?.catalog?.name.equals("Hulu US") }.take(4)
                    "Amazon Prime Video" -> bundle.programs.filter { it.availableOn?.firstOrNull()?.catalog?.name.equals("Amazon PV US") }.take(4)
//                    "Paramount" -> bundle.programs.filter { it.availableOn?.firstOrNull()?.catalog?.name.equals("Paramount Plus US") }.take(4)
                    else -> bundle.programs.take(4)
                }
//                val topPrograms = bundle.programs.take(4) // 최대 4개 BundleProgram
                Log.d("mjpark", "topPrograms : $topPrograms ")
                bundleName to topPrograms
            }

        val genreName = bundleToProgramNames.keys.toList()[position]
        holder.tvGenre.text = genreName

        val programList = bundleToProgramNames[genreName]?.take(4) ?: emptyList()

        val movieAdapter = ProgramBundleAdapter(programList,
            genreType,
            onItemClick)
        holder.rvMovieList.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = movieAdapter
        }

        holder.ivArrow.setOnClickListener {
            onArrowClick(genreName)
        }

    }

    override fun getItemCount() = genreList.size
}
