// NewsApiService.kt
package com.example.a3dmodelsample.retrofit

import com.example.a3dmodelsample.retrofit.data.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    // Get top headlines by country
    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country") country: String = "kr",
        @Query("apiKey") apiKey: String,
        @Query("category") category: String? = null,
        @Query("page") page: Int? = null,
        @Query("pageSize") pageSize: Int? = null
    ): NewsResponse

    // Search all news articles
    @GET("everything")
    suspend fun searchNews(
        @Query("q") keyword: String,
        @Query("apiKey") apiKey: String,
        @Query("from") from: String? = null,
        @Query("to") to: String? = null,
        @Query("sortBy") sortBy: String? = "publishedAt",
        @Query("page") page: Int? = null,
        @Query("pageSize") pageSize: Int? = null,
        @Query("language") language: String = "ko"
    ): NewsResponse
}
