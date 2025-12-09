package com.example.a3dmodelsample.retrofit

object RetrofitClient {

    fun createGraceNote(apiKey: String): GracenoteApi =
        GracenoteClient.create(apiKey)

    fun createNewsApi(apiKey: String): NewsApiService =
        NewsApiClient.create(apiKey)

//    fun createWeatherApi(): WeatherApiService =
//        WeatherApiClient.create()
}