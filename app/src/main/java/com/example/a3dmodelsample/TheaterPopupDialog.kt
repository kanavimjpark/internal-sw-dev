package com.example.a3dmodelsample

import android.app.Dialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL
import androidx.core.net.toUri

class TheaterPopupDialog(context: Context) : Dialog(context) {

    private var imageUrl: String = ""
    private var title: String = ""
    private var message: String = ""
    private var summary: String = ""
    private var onConfirmClick: (() -> Unit)? = null

    private lateinit var ivBanner: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvInfo: TextView
    private lateinit var tvSummary: TextView

    private lateinit var btNetflix: ImageButton
    private lateinit var btDisney: ImageButton
    private lateinit var btHulu: ImageButton
    private lateinit var btPrime: ImageButton
    private lateinit var btParamount: ImageButton

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.theater_contents_popup)

        // setContentView 이후에 findViewById
        ivBanner = findViewById(R.id.iv_content_image)
        tvTitle = findViewById(R.id.tvTitle)
        tvInfo = findViewById(R.id.tvInfo)
        tvSummary = findViewById(R.id.tvSummary)

        btDisney = findViewById(R.id.bt_disney)
        btNetflix = findViewById(R.id.bt_netflix)
        btHulu = findViewById(R.id.bt_hulu)
        btPrime = findViewById(R.id.bt_prime_video)
        btParamount = findViewById(R.id.bt_paramount_video)

        tvTitle.text = title
        tvInfo.text = message
        tvSummary.text = summary

    }

    fun setBanner(url: String): TheaterPopupDialog {
        this.imageUrl = url

        if (imageUrl.isNotEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val inputStream = URL(imageUrl).openStream()
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    // 메인 스레드에서 ImageView 세팅
                    CoroutineScope(Dispatchers.Main).launch {
                        if (::ivBanner.isInitialized) {
                            ivBanner.setImageBitmap(bitmap)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    CoroutineScope(Dispatchers.Main).launch {
                        if (::ivBanner.isInitialized) {
                            ivBanner.setImageDrawable(null)
                        }
                    }
                }
            }
        } else {
            if (::ivBanner.isInitialized) ivBanner.setImageDrawable(null)
        }
        return this
    }

    fun setTitleText(title: String): TheaterPopupDialog {
        this.title = title
        if (::tvTitle.isInitialized) tvTitle.text = title
        return this
    }

    fun setMessageText(message: String): TheaterPopupDialog {
        this.message = message
        if (::tvInfo.isInitialized) tvInfo.text = message
        return this
    }

    fun setSummaryText(summary: String): TheaterPopupDialog {
        this.summary = summary
        if (::tvSummary.isInitialized) tvSummary.text = summary
        return this
    }

    fun setStreamingLinks(links: Map<String, String>): TheaterPopupDialog {

        Log.d("mjpark","Link list : $links")
        // 넷플릭스
        links["netflix"]?.let { url ->
            btNetflix.visibility = View.VISIBLE
            btNetflix.setOnClickListener {
                openWeb(url)
            }
        }

        // 디즈니
        links["disney"]?.let { url ->
            btDisney.visibility = View.VISIBLE
            btDisney.setOnClickListener {
                openWeb(url)
            }
        }

        // 훌루
        links["hulu"]?.let { url ->
            btHulu.visibility = View.VISIBLE
            btHulu.setOnClickListener {
                openWeb(url)
            }
        }

        // 프라임비디오
        links["prime"]?.let { url ->
            btPrime.visibility = View.VISIBLE
            btPrime.setOnClickListener {
                openWeb(url)
            }
        }

        links["paramountplus"]?.let { url ->
            btParamount.visibility = View.VISIBLE
            btParamount.setOnClickListener {
                openWeb(url)
            }
        }
        return this
    }

    private fun openWeb(url: String) {
        val intent = Intent().apply {
            component = ComponentName(
                "com.example.webview_test",
                "com.example.webview_test.MainActivity"
            )
            putExtra("com.example.webview_test.EXTRA_URL", url)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            Log.w("mjpark", "send URL = $url")
        }
        context.startActivity(intent)
        dismiss()
    }

    override fun dismiss() {
        super.dismiss()
        Log.d("mjpark", "dismiss!@@!")
    }
}

