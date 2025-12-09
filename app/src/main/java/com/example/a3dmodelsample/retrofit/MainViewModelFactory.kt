// MainViewModelFactory.kt
package com.example.a3dmodelsample.retrofit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.a3dmodelsample.retrofit.NewsRepository
import com.example.a3dmodelsample.retrofit.WeatherRepository
import com.example.a3dmodelsample.viewmodel.MainViewModel

class MainViewModelFactory(
    private val weatherRepository: WeatherRepository,
    private val newsRepository: NewsRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            return MainViewModel(weatherRepository, newsRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: $modelClass")
    }
}
