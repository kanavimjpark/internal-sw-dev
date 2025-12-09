package com.example.a3dmodelsample.retrofit

object NewsApiClient {
    private const val BASE_URL = "https://newsapi.org/v2/"

    fun create(apiKey: String): NewsApiService {
        return RetrofitFactory.createService(
            baseUrl = BASE_URL
        ) {
            // This block configures OkHttpClient.Builder for NewsAPI only
            addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .addHeader("X-Api-Key", apiKey)   // or use query param if you prefer
                    .build()
                chain.proceed(req)
            }
        }
    }
}
