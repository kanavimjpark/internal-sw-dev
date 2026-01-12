package com.example.a3dmodelsample.retrofit

import com.example.a3dmodelsample.retrofit.data.AlphaVantageGlobalQuoteResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AlphaVantageApiService {
    @GET("query")
    suspend fun globalQuote(
        @Query("function") function: String = "GLOBAL_QUOTE",
        @Query("symbol") symbol: String,
        @Query("apikey") apiKey: String
    ): AlphaVantageGlobalQuoteResponse
}
