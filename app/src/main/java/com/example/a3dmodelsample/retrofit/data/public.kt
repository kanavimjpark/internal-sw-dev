package com.example.a3dmodelsample.retrofit.data

// Meta & References
data class Meta(
    val total: Int,
    val version: String,
    val references: References
)

data class References(
    val preferredLanguage: String?,
    val contentMarket: String?,
    val leagueID: String?,
    val teamID: String?,
    val personID: String?,
    val divisionID: String?,
    val conferenceID: String?,
    val overallID: String?,
    val matchID: String?,
    val omitCatalogIDs: String?,
    val bundleID: String?,
    val programID: String?,
    val catalogID: String?,
    val phaseID: String?,
    val artistID: String?,
    val albumEditionID: String?,
    val recordingID: String?,
    val artistName: String?,
    val albumEditionName: String?,
    val recordingName: String?
)

// 공통 객체들
data class ProgramUrl(
    val URL: String,
    val type: String
)

data class Image(
    val orientation: String?,
    val type: String?,
    val URL: String?
)

data class Catalog(
    val name: String?,
    val catalogID: String?,
    val catalogType: String?,
    val images: List<Image>?
)

data class AvailableOn(
    val URLs: List<ProgramUrl>?,
    val externalID: String?,
    val catalog: Catalog?
)

