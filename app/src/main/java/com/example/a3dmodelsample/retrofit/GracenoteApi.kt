package com.example.a3dmodelsample.retrofit

import com.example.a3dmodelsample.retrofit.data.ProgramBundleResponse
import com.example.a3dmodelsample.retrofit.data.ProgramResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface GracenoteApi {
    // 단일 프로그램 상세 조회
    @GET("programs/{programID}")
    suspend fun getProgram(
        @Path("programID") programID: String,
        @Query("contentMarket") contentMarket: String = "USA",
        @Query("preferredLanguage") language: String = "en-GB"
    ): ProgramResponse

    // 프로그램 번들 조회
    @GET("programBundles")
    suspend fun getProgramBundles(
        @Query("contentMarket") contentMarket: String = "USA",
        @Query("preferredLanguage") language: String = "en-GB"
    ): ProgramBundleResponse
}

