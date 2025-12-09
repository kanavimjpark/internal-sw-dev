package com.example.a3dmodelsample

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [BluetoothFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class BluetoothFragment : Fragment(R.layout.fragment_bluetooth) {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MusicListAdapter
    private lateinit var lvMute: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView = view.findViewById(R.id.rv_list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        lvMute = view.findViewById(R.id.mute)

        val dummyMusicList = listOf(
            MusicItem(1,R.drawable.song_noimg_light , "Sunrise Drive", "Lofi Breeze", "Morning Vibes", 189000L),
            MusicItem(2, R.drawable.song_noimg_light, "Night Chill", "Dream Wave", "Moonlight Jazz", 224000L),
            MusicItem(3, R.drawable.song_noimg_light, "Coffee and Code", "Byte Loops", "Dev Focus", 200000L),
            MusicItem(4, R.drawable.song_noimg_light, "Cloud City", "Synth Skies", "Retro Future", 250000L),
            MusicItem(5, R.drawable.song_noimg_light, "Raindrop Riddim", "Echo Pulse", "Lo-Fi Storm", 178000L),
            MusicItem(6, R.drawable.song_noimg_light, "Neon Rain", "Chroma Sound", "City Lights", 210000L),
            MusicItem(7, R.drawable.song_noimg_light, "Lazy Sunday", "Warm Keys", "Couch Beats", 195000L),
            MusicItem(8, R.drawable.song_noimg_light, "Code Flow", "Bit Rhythm", "Focus Mode", 230000L),
            MusicItem(9, R.drawable.song_noimg_light, "Ocean Eyes", "Soft Analog", "Dreamy Waves", 205000L),
            MusicItem(10, R.drawable.song_noimg_light, "Winter Walk", "Frost Notes", "Cold Breeze", 199000L),
            MusicItem(11, R.drawable.song_noimg_light, "Deep Space", "Orbit Crew", "Galactic Chill", 245000L),
            MusicItem(12, R.drawable.song_noimg_light, "Rustic Mood", "Woodland Echo", "Nature Flow", 185000L),
            MusicItem(13, R.drawable.song_noimg_light, "Digital Sunset", "Pixel Fade", "Lo-Bit Dreams", 220000L),
            MusicItem(14, R.drawable.song_noimg_light, "Metro Rush", "Train Trackers", "Urban Beats", 201000L),
            MusicItem(15, R.drawable.song_noimg_light, "Serenity Stream", "Calm Current", "Water Flow", 212000L),
            MusicItem(16, R.drawable.song_noimg_light, "Evening Brew", "Cafe Soul", "Acoustic Coffee", 198000L),
            MusicItem(17, R.drawable.song_noimg_light, "Electric Horizon", "Neon Pulse", "Future Tone", 232000L),
            MusicItem(18, R.drawable.song_noimg_light, "Quiet Coding", "Loop Breakers", "Silent Mode", 180000L),
            MusicItem(19, R.drawable.song_noimg_light, "Island Haze", "Tropic Beat", "Sunset Lounge", 190000L),
            MusicItem(20, R.drawable.song_noimg_light, "Hollow Echo", "Ghost Notes", "Phantom Jazz", 238000L)
        )


        adapter = MusicListAdapter(dummyMusicList)
        recyclerView.adapter = adapter


        lvMute.setOnClickListener {
            requireActivity().finish()
        }

    }

}