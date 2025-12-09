// MainViewModel.kt
package com.example.a3dmodelsample.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a3dmodelsample.retrofit.NewsRepository
import com.example.a3dmodelsample.retrofit.WeatherRepository
import com.example.a3dmodelsample.retrofit.data.DailyWeatherUiModel
import com.example.a3dmodelsample.retrofit.data.ForecastResponse
import com.example.a3dmodelsample.retrofit.data.FutureWeatherResponse
import com.example.a3dmodelsample.retrofit.data.HourlyWeatherUi
import com.example.a3dmodelsample.retrofit.data.WeatherResponse
import com.example.a3dmodelsample.retrofit.data.NewsResponse
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class MainViewModel(
    private val weatherRepo: WeatherRepository,
    private val newsRepo: NewsRepository
) : ViewModel() {

    val weatherLiveData = MutableLiveData<WeatherResponse>()
//    val threeHourStepWeatherLiveData = MutableLiveData<ForecastResponse>()

    private val _threeHourStepWeatherLiveData = MutableLiveData<List<HourlyWeatherUi>>()
    val threeHourStepWeatherLiveData: MutableLiveData<List<HourlyWeatherUi>> = _threeHourStepWeatherLiveData

    private val _weekWeather = MutableLiveData<List<DailyWeatherUiModel>>()
    val weekWeather: MutableLiveData<List<DailyWeatherUiModel>> = _weekWeather

    val newsLiveData = MutableLiveData<NewsResponse>()
    val errorLiveData = MutableLiveData<String>()

    val zoneId = ZoneId.of("Asia/Seoul")
    val today = LocalDate.now(zoneId)
    val startDate = today
    val endDate = today.plusDays(6)

    val startDateStr = startDate.toString()
    val endDateStr = endDate.toString()


    fun loadWeather(city: String) {
        viewModelScope.launch {
            try {
                val data = weatherRepo.getWeatherByCity(city)
                weatherLiveData.postValue(data)
            } catch (e: Exception) {
                errorLiveData.postValue("Weather error: ${e.message}")
            }
        }
    }

    fun loadNews(country: String = "us", category: String? = null) {
        viewModelScope.launch {
            try {
                val data = newsRepo.fetchTopHeadlines(country, category)
                newsLiveData.postValue(data)
            } catch (e: Exception) {
                errorLiveData.postValue("News error: ${e.message}")
            }
        }
    }

    fun loadThreeHourStepWeather(){
        viewModelScope.launch {
            try {
                val forecast = weatherRepo.getThreeHourForecastByCity("Seoul,KR")
                threeHourStepWeatherLiveData.postValue(forecast)
            } catch (e: Exception) {
                errorLiveData.postValue("Weather error: ${e.message}")
            }
        }
    }

    fun getWeekWeather(){
        viewModelScope.launch {
            try {
                val result = weatherRepo.getArchiveWeather(37.55, 127.0)
                weekWeather.postValue(result)
            } catch (e: Exception) {
                Log.e("Weather", "Unknown error", e)
            }
        }
    }

}
