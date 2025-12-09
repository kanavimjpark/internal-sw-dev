package com.example.a3dmodelsample.retrofit.data


data class ProgramResponse(
    val meta: Meta,
    val data: List<Program>
)

data class Program(
    val programID: String,
    val name: String,
    val nameShort: String?,
    val description: String?,
    val programType: String?,
    val programSubType: String?,
    val rating: String?,
    val country: String?,
    val releaseYear: Int?,
    val durationMilliseconds: Long?,
    val descriptors: Descriptors?,
    val availableOn: List<AvailableOn>?,
    val images: List<Image>?
)

data class Descriptors(
    val genres: List<Genre>?
)

data class Genre(
    val name: String,
    val descriptorID: String
)
