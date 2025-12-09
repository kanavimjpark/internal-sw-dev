package com.example.a3dmodelsample

data class MusicItem(
    val index: Int,             // 리스트 인덱스
    val albumArt: Int,      // 앨범 아트 (nullable)
    val title: String,          // 곡 제목
    val artist: String,         // 아티스트 이름
    val album: String,          // 앨범 이름
    val duration: Long          // 재생 시간 (밀리초 단위)
)



