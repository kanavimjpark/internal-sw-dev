// NewsRepository.kt
package com.example.a3dmodelsample.retrofit

import android.util.Log
import com.example.a3dmodelsample.retrofit.NewsApiService
import com.example.a3dmodelsample.retrofit.data.NewsResponse

class NewsRepository(
    private val api: NewsApiService,
    private val apiKey: String
) {

    // Fetch top headlines (by country / optional category)
    suspend fun fetchTopHeadlines(
        country: String = "us",
        category: String? = null,
        page: Int? = null,
        pageSize: Int? = null
    ): NewsResponse {
        // Call NewsAPI
        val resp = api.getTopHeadlines(
            country = country,
            apiKey = apiKey,
            category = category,
            page = page,
            pageSize = pageSize
        )
        Log.d("NewsRepository", "fetchTopHeadlines result: $resp")
        return resp
    }

    // Search news by keyword
    suspend fun fetchNewsByKeyword(
        keyword: String,
        from: String? = null,
        to: String? = null,
        sortBy: String? = "publishedAt",
        page: Int? = null,
        pageSize: Int? = null,
        language: String = "ko"
    ): NewsResponse {
        val resp = api.searchNews(
            keyword = keyword,
            apiKey = apiKey,
            from = from,
            to = to,
            sortBy = sortBy,
            page = page,
            pageSize = pageSize,
            language = language
        )
        Log.d("NewsRepository", "fetchNewsByKeyword result: $resp")
        return resp
    }
}
