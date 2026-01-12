// MainViewModel.kt
package com.example.a3dmodelsample.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a3dmodelsample.retrofit.EtfRepository
import com.example.a3dmodelsample.retrofit.NewsRepository
import com.example.a3dmodelsample.retrofit.WeatherRepository
import com.example.a3dmodelsample.retrofit.data.DailyWeatherUiModel
import com.example.a3dmodelsample.retrofit.data.EtfQuoteUi
import com.example.a3dmodelsample.retrofit.data.HourlyWeatherUi
import com.example.a3dmodelsample.retrofit.data.WeatherResponse
import com.example.a3dmodelsample.retrofit.data.NewsResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainViewModel(
    private val weatherRepo: WeatherRepository,
    private val newsRepo: NewsRepository,
    private val etfRepo: EtfRepository
) : ViewModel() {

    val weatherLiveData = MutableLiveData<WeatherResponse>()

    private val _threeHourStepWeatherLiveData = MutableLiveData<List<HourlyWeatherUi>>()
    val threeHourStepWeatherLiveData: MutableLiveData<List<HourlyWeatherUi>> = _threeHourStepWeatherLiveData

    private val _weekWeather = MutableLiveData<List<DailyWeatherUiModel>>()
    val weekWeather: MutableLiveData<List<DailyWeatherUiModel>> = _weekWeather

    val newsLiveData = MutableLiveData<NewsResponse>()
    val errorLiveData = MutableLiveData<String>()

    private val _etfData = MutableLiveData<List<EtfQuoteUi>>()
    val etfData: MutableLiveData<List<EtfQuoteUi>> = _etfData



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

    fun getETFData() {
        viewModelScope.launch {
            try {
                val data = etfRepo.fetchMajorEtfQuotes()
                etfData.postValue(data)
            } catch (e: Exception) {
                errorLiveData.postValue("Stock error: ${e.message}")
            }
        }
    }


}
