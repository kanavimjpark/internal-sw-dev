package com.example.a3dmodelsample

import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [VideoFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VideoFragment : Fragment(R.layout.fragment_video) {

    private lateinit var tabVideo: TextView
    private lateinit var tabTheater: TextView
    private lateinit var tabFavorite: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabVideo = view.findViewById(R.id.tabVideo)
        tabTheater = view.findViewById(R.id.tabTheater)
        tabFavorite = view.findViewById(R.id.tabFavorite)

        tabVideo.setOnClickListener {
            selectTab("Video")
        }

        tabTheater.setOnClickListener {
            selectTab("Theater")
        }

        tabFavorite.setOnClickListener {
            selectTab("Favorite")
        }

        // 기본으로 USB 탭 선택
        selectTab("Video")

    }

    private fun selectTab(type: String) {
        when (type) {
            "Video" -> {
                tabVideo.setTextColor(ContextCompat.getColor(requireContext(), R.color.c_1D1D1D))
                tabVideo.setTypeface(null, Typeface.BOLD)
                tabTheater.setTextColor(ContextCompat.getColor(requireContext(), R.color.c_555555))
                tabTheater.setTypeface(null, Typeface.NORMAL)
                tabFavorite.setTextColor(ContextCompat.getColor(requireContext(), R.color.c_555555))
                tabFavorite.setTypeface(null, Typeface.NORMAL)

                childFragmentManager.beginTransaction()
                    .replace(R.id.mediaTabContent, BluetoothFragment())
                    .commit()
            }

            "Theater" -> {
                tabTheater.setTextColor(ContextCompat.getColor(requireContext(), R.color.c_1D1D1D))
                tabTheater.setTypeface(null, Typeface.BOLD)
                tabVideo.setTextColor(ContextCompat.getColor(requireContext(), R.color.c_555555))
                tabVideo.setTypeface(null, Typeface.NORMAL)
                tabFavorite.setTextColor(ContextCompat.getColor(requireContext(), R.color.c_555555))
                tabFavorite.setTypeface(null, Typeface.NORMAL)

                childFragmentManager.beginTransaction()
                    .replace(R.id.mediaTabContent, TheaterFragment())
                    .commit()
            }

            "Favorite" -> {
                tabFavorite.setTextColor(ContextCompat.getColor(requireContext(), R.color.c_1D1D1D))
                tabFavorite.setTypeface(null, Typeface.BOLD)
                tabVideo.setTextColor(ContextCompat.getColor(requireContext(), R.color.c_555555))
                tabVideo.setTypeface(null, Typeface.NORMAL)
                tabTheater.setTextColor(ContextCompat.getColor(requireContext(), R.color.c_555555))
                tabTheater.setTypeface(null, Typeface.NORMAL)

                childFragmentManager.beginTransaction()
                    .replace(R.id.mediaTabContent, FavoriteFragment())
                    .commit()
            }
        }
    }

}

