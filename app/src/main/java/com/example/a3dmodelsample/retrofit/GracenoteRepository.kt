package com.example.a3dmodelsample.retrofit

import android.util.Log
import com.example.a3dmodelsample.retrofit.data.Program
import com.example.a3dmodelsample.retrofit.data.ProgramBundle

class GracenoteRepository(private val api: GracenoteApi) {

    suspend fun fetchProgramBundles(
        contentMarket: String = "USA",
        language: String = "en-GB"
    ): List<ProgramBundle> {
        val resp = api.getProgramBundles(contentMarket, language)
        Log.d("mjpark", "fetchProgramBundles 결과: $resp")
        return resp.data
    }


    // 특정 programID 로 상세(또는 목록) 가져오기 - API가 list로 내려오므로 List<Program> 반환
    suspend fun fetchProgramById(
        programId: String,
        contentMarket: String = "USA",
        language: String = "en-GB"
    ): List<Program> {
        val resp = api.getProgram(programId, contentMarket, language)
        Log.d("mjpark", "fetchProgramById 결과: $resp")
        return resp.data
    }
}