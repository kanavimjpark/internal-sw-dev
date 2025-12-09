package com.example.a3dmodelsample.retrofit

import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object HttpClientProvider {

    // Shared dispatcher for all HTTP requests
    private val dispatcher: Dispatcher by lazy {
        Dispatcher().apply {
            maxRequests = 32            // Global concurrent network limit
            maxRequestsPerHost = 6      // Per-host limit
        }
    }

    // Shared logging interceptor
    private val logging: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    // Base OkHttpClient.Builder with common configuration
    fun baseBuilder(): OkHttpClient.Builder =
        OkHttpClient.Builder()
            .dispatcher(dispatcher)
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
}
