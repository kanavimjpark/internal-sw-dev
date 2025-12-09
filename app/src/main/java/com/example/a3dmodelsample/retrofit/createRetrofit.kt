package com.example.a3dmodelsample.retrofit

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitFactory {

    // Create Retrofit with optional extra configuration for OkHttpClient.Builder
    fun createRetrofit(
        baseUrl: String,
        clientConfig: (OkHttpClient.Builder.() -> Unit)? = null
    ): Retrofit {
        val builder = HttpClientProvider.baseBuilder()
        clientConfig?.invoke(builder)

        val client = builder.build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Generic service creator
    inline fun <reified T> createService(
        baseUrl: String,
        noinline clientConfig: (OkHttpClient.Builder.() -> Unit)? = null
    ): T {
        return createRetrofit(baseUrl, clientConfig).create(T::class.java)
    }
}
