package com.example.a3dmodelsample.retrofit

object GracenoteClient {
    private const val BASE_URL = "https://api.nexus.gracenote.com/v1/"

    // You can inject this key from BuildConfig or any secure source
    fun create(apiKey: String): GracenoteApi {
        return RetrofitFactory.createService(
            baseUrl = BASE_URL
        ) {
            // This block configures OkHttpClient.Builder for Gracenote only
            addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .addHeader("GN-APIKEY", apiKey)
                    .build()
                chain.proceed(req)
            }
        }
    }
}
