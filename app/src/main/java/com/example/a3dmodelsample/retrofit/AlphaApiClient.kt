package com.example.a3dmodelsample.retrofit

import okhttp3.HttpUrl


object AlphaApiClient {

    private const val BASE_URL = "https://www.alphavantage.co/"

    fun create(apiKey: String): AlphaVantageApiService {
        return RetrofitFactory.createService(
            baseUrl = BASE_URL
        ) {
            addInterceptor { chain ->
                val original = chain.request()
                val originalUrl = original.url

                val newUrl = if (originalUrl.queryParameter("apikey") == null) {
                    originalUrl.newBuilder()
                        .addQueryParameter("apikey", apiKey)
                        .build()
                } else {
                    originalUrl
                }

                val newReq = original.newBuilder()
                    .url(newUrl)
                    .build()

                chain.proceed(newReq)
            }
        }
    }
}
