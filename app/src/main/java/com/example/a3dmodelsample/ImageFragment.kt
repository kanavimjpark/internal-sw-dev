package com.example.a3dmodelsample

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.a3dmodelsample.retrofit.GracenoteRepository
import com.example.a3dmodelsample.retrofit.MainViewModelFactory
import com.example.a3dmodelsample.retrofit.NewsRepository
import com.example.a3dmodelsample.retrofit.RetrofitClient
import com.example.a3dmodelsample.retrofit.WeatherRepository
import com.example.a3dmodelsample.retrofit.data.DailyWeatherUiModel
import com.example.a3dmodelsample.retrofit.data.FutureWeatherResponse
import com.example.a3dmodelsample.retrofit.data.ProgramBundle
import com.example.a3dmodelsample.retrofit.data.WeatherResponse
import com.example.a3dmodelsample.viewmodel.MainViewModel

class ImageFragment : Fragment(R.layout.fragment_image) {

    private lateinit var tvHumidity: TextView
    private lateinit var tvWind: TextView
    private lateinit var tvPressure: TextView
    private lateinit var tvPrecipitation: TextView
    private lateinit var tvDegree: TextView
    private lateinit var mainViewModel: MainViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: WeatherAdapter
    private lateinit var graphAdapter: TempGraphAdapter
    private lateinit var tempGraphRecyclerview: RecyclerView
    private lateinit var layout: ConstraintLayout


    private val NEWS_API_KEY = "dd07ab437c704c74babae5f73df37976"
    private val WEATHER_API_KEY = "16b7d1ccd4c3e4f4f42e2051cb5fe5dd"


    private fun setupViewModel() {
        // Create API clients
        val newsApi = RetrofitClient.createNewsApi(NEWS_API_KEY)

        // Create repositories
        val weatherRepo = WeatherRepository(
            apiKey = WEATHER_API_KEY
        )
        val newsRepo = NewsRepository(
            api = newsApi,
            apiKey = NEWS_API_KEY
        )

        // Create ViewModel via factory
        val factory = MainViewModelFactory(weatherRepo, newsRepo)
        mainViewModel = ViewModelProvider(this, factory)[MainViewModel::class.java]
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupViewModel()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layout = view.findViewById(R.id.container_weather)
        tvHumidity = view.findViewById(R.id.tv_humidity_value)
        tvPressure = view.findViewById(R.id.tv_pressure_value)
        tvWind = view.findViewById(R.id.tv_wind_value)
        tvPrecipitation = view.findViewById(R.id.tv_precipitatino_value)
        tvDegree = view.findViewById(R.id.tv_degree)

        recyclerView = view.findViewById(R.id.rv_weather)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        tempGraphRecyclerview = view.findViewById(R.id.rv_3hDiff)
        tempGraphRecyclerview.layoutManager = LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        mainViewModel.loadWeather("Seoul,KR")

        mainViewModel.weatherLiveData.observe(viewLifecycleOwner) { bundles ->
            Log.d("MainActivity", "🌤 Weather Temp = $bundles")

            tvWind.text = "${bundles.wind?.speed ?: 0.0} M/S"
            tvHumidity.text = "${bundles.main?.humidity ?: 0} %"
            tvPressure.text = "${bundles.main?.pressure ?: 0} Mm"
            tvPrecipitation.text = "${bundles.main?.temp ?: 0.0} %"
            tvDegree.text = "${bundles.main?.temp ?: 0.0} °"

            val iconCode = bundles.weather?.firstOrNull()?.main
            val iconRes = when(iconCode) {
                "Clear" -> R.drawable.sunny
                "Clouds" -> R.drawable.cloudy
                "Rain" -> R.drawable.rainy
                else -> {
                    Log.d("tempMapper","$iconCode")
                    R.drawable.snowing
                }
            }
            layout.setBackgroundResource(iconRes)
        }

        mainViewModel.getWeekWeather()


        mainViewModel.weekWeather.observe(viewLifecycleOwner) { it ->
            adapter = WeatherAdapter(it)
            recyclerView.adapter = adapter

        }

        mainViewModel.loadThreeHourStepWeather()
        mainViewModel.threeHourStepWeatherLiveData.observe(viewLifecycleOwner) { it ->
            graphAdapter = TempGraphAdapter(it)
            tempGraphRecyclerview.adapter = graphAdapter
        }

    }
}