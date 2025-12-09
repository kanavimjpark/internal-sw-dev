package com.example.a3dmodelsample

import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

class MusicActivity : AppCompatActivity() {
    private lateinit var musicIcon: ImageView
    private lateinit var radioIcon: ImageView
    private lateinit var videoIcon: ImageView
    private lateinit var imageIcon: ImageView
    private lateinit var musicText: TextView
    private lateinit var radioText: TextView
    private lateinit var videoText: TextView
    private lateinit var imageText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_music)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        musicIcon = findViewById(R.id.icon_music)
        radioIcon = findViewById(R.id.icon_radio)
        videoIcon = findViewById(R.id.icon_video)
        imageIcon = findViewById(R.id.icon_image)
        musicText = findViewById(R.id.text_music)
        radioText = findViewById(R.id.text_radio)
        videoText = findViewById(R.id.text_video)
        imageText = findViewById(R.id.text_image)

        findViewById<LinearLayout>(R.id.tab_music).setOnClickListener {
            selectTab(R.id.tab_music)
        }

        findViewById<LinearLayout>(R.id.tab_radio).setOnClickListener {
            selectTab(R.id.tab_radio)
        }

        findViewById<LinearLayout>(R.id.tab_video).setOnClickListener {
            selectTab(R.id.tab_video)
        }

        findViewById<LinearLayout>(R.id.tab_image).setOnClickListener {
            selectTab(R.id.tab_image)
        }
        // 초기 화면
        selectTab(R.id.tab_music)
    }

    private fun selectTab(selectedId: Int) {
        when (selectedId) {
            R.id.tab_music -> {
                musicIcon.setImageResource(R.drawable.selected_media2)
                radioIcon.setImageResource(R.drawable.radio)
                videoIcon.setImageResource(R.drawable.video)
                imageIcon.setImageResource(R.drawable.image)

                musicText.setTextColor(ContextCompat.getColor(this, R.color.c_FF5D00))
                radioText.setTextColor(ContextCompat.getColor(this, R.color.c_555555))
                videoText.setTextColor(ContextCompat.getColor(this, R.color.c_555555))
                imageText.setTextColor(ContextCompat.getColor(this, R.color.c_555555))
                replaceFragment(MusicFragment())
            }

            R.id.tab_radio -> {
                musicIcon.setImageResource(R.drawable.media2)
                radioIcon.setImageResource(R.drawable.selected_radio)
                videoIcon.setImageResource(R.drawable.video)
                imageIcon.setImageResource(R.drawable.image)

                musicText.setTextColor(ContextCompat.getColor(this, R.color.c_555555))
                radioText.setTextColor(ContextCompat.getColor(this, R.color.c_FF5D00))
                videoText.setTextColor(ContextCompat.getColor(this, R.color.c_555555))
                imageText.setTextColor(ContextCompat.getColor(this, R.color.c_555555))
                replaceFragment(RadioFragment())
            }

            R.id.tab_video -> {
                musicIcon.setImageResource(R.drawable.media2)
                radioIcon.setImageResource(R.drawable.radio)
                videoIcon.setImageResource(R.drawable.selected_video)
                imageIcon.setImageResource(R.drawable.image)

                musicText.setTextColor(ContextCompat.getColor(this, R.color.c_555555))
                radioText.setTextColor(ContextCompat.getColor(this, R.color.c_555555))
                videoText.setTextColor(ContextCompat.getColor(this, R.color.c_FF5D00))
                imageText.setTextColor(ContextCompat.getColor(this, R.color.c_555555))
                replaceFragment(VideoFragment())
            }

            R.id.tab_image -> {
                musicIcon.setImageResource(R.drawable.media2)
                radioIcon.setImageResource(R.drawable.radio)
                videoIcon.setImageResource(R.drawable.video)
                imageIcon.setImageResource(R.drawable.selected_image)

                musicText.setTextColor(ContextCompat.getColor(this, R.color.c_555555))
                radioText.setTextColor(ContextCompat.getColor(this, R.color.c_555555))
                videoText.setTextColor(ContextCompat.getColor(this, R.color.c_555555))
                imageText.setTextColor(ContextCompat.getColor(this, R.color.c_FF5D00))
                replaceFragment(ImageFragment())
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

}


