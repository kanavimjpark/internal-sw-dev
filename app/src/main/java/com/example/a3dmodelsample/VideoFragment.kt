package com.example.a3dmodelsample

import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a3dmodelsample.retrofit.EtfRepository
import com.example.a3dmodelsample.retrofit.MainViewModelFactory
import com.example.a3dmodelsample.retrofit.NewsRepository
import com.example.a3dmodelsample.retrofit.RetrofitClient
import com.example.a3dmodelsample.retrofit.WeatherRepository
import com.example.a3dmodelsample.viewmodel.MainViewModel

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
    private lateinit var tvDegree: TextView
    private lateinit var tvDescription: TextView

    private lateinit var mainViewModel: MainViewModel
    private lateinit var rvWeatherList: RecyclerView
    private lateinit var rvNewsList: RecyclerView
    private lateinit var rvStockList: RecyclerView
    private lateinit var dailyBriefingWeatherAdapter: DailyBriefingWeatherAdapter
    private lateinit var newsAdapter: NewsAdapter
    private lateinit var stockAdapter: DailyBriefingStockAdapter

    private val NEWS_API_KEY = "dd07ab437c704c74babae5f73df37976"
    private val WEATHER_API_KEY = "16b7d1ccd4c3e4f4f42e2051cb5fe5dd"
    private val ALPHA_STOCK_API_KEY = "M19CY7MOHU7ZJ0WP"

    private fun setupViewModel() {
        // Create API clients
        val newsApi = RetrofitClient.createNewsApi(NEWS_API_KEY)
        val etfApi = RetrofitClient.createStockApi(ALPHA_STOCK_API_KEY)

        // Create repositories
        val weatherRepo = WeatherRepository(
            apiKey = WEATHER_API_KEY
        )
        val newsRepo = NewsRepository(
            api = newsApi,
            apiKey = NEWS_API_KEY
        )

        val etfRepo = EtfRepository(
            api = etfApi,
            apiKey = ALPHA_STOCK_API_KEY
        )

        // Create ViewModel via factory
        val factory = MainViewModelFactory(weatherRepo, newsRepo, etfRepo)
        mainViewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        tabVideo = view.findViewById(R.id.tabVideo)
        tabTheater = view.findViewById(R.id.tabTheater)
        tabFavorite = view.findViewById(R.id.tabFavorite)
        tvDegree = view.findViewById(R.id.tv_degree)
        tvDescription = view.findViewById(R.id.tv_description)
        rvWeatherList = view.findViewById(R.id.rv_weather_list)
        rvNewsList = view.findViewById(R.id.rv_news_list)
        rvStockList = view.findViewById(R.id.rv_stock_list)

        rvWeatherList.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        rvNewsList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        rvStockList.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
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

                mainViewModel.loadThreeHourStepWeather()
                mainViewModel.threeHourStepWeatherLiveData.observe(viewLifecycleOwner) { it ->
                    dailyBriefingWeatherAdapter = DailyBriefingWeatherAdapter(it)
                    rvWeatherList.adapter = dailyBriefingWeatherAdapter
                }

                mainViewModel.loadWeather("New York,US")

                mainViewModel.weatherLiveData.observe(viewLifecycleOwner) { bundles ->
                    Log.d("MainActivity", "🌤 Weather Temp = $bundles")

                    tvDegree.text = "${bundles.main?.temp ?: 0.0} °c"
                    tvDescription.text = "${bundles.weather?.firstOrNull()?.main} "
                }
                mainViewModel.loadNews("us")
                mainViewModel.newsLiveData.observe(viewLifecycleOwner) { news ->
                    val articles = news.articles.orEmpty()
                    Log.d("MainActivity", "📰 Top Headline = ${news.articles?.size}")

                    newsAdapter = NewsAdapter(articles)
                    rvNewsList.adapter = newsAdapter
                }
                mainViewModel.getETFData()
                mainViewModel.etfData.observe(viewLifecycleOwner) {
                    stockAdapter = DailyBriefingStockAdapter(it)
                    rvStockList.adapter = stockAdapter
                }
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

