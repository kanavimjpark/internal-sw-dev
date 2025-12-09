package com.example.a3dmodelsample.retrofit.data


data class ProgramBundleResponse(
    val meta: Meta,
    val data: List<ProgramBundle>
)

data class ProgramBundle(
    val bundleID: String,
    val name: String,
    val lastUpdatedUTC: String,
    val bundleType: String,
    val programs: List<BundleProgram>
)

data class BundleProgram(
    val programID: String,
    val name: String,
    val nameShort: String?,
    val durationMilliseconds: Long?,
    val availableOn: List<AvailableOn>?,
    val images: List<Image>?
)
