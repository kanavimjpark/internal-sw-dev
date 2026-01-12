package com.example.a3dmodelsample.retrofit.data

import com.google.gson.annotations.SerializedName

data class AlphaVantageGlobalQuoteResponse(
    @SerializedName("Global Quote")
    val globalQuote: GlobalQuote?
)

data class GlobalQuote(
    @SerializedName("01. symbol") val symbol: String?,
    @SerializedName("05. price") val price: String?,
    @SerializedName("09. change") val change: String?,
    @SerializedName("10. change percent") val changePercent: String?
)

data class EtfQuoteUi(
    val displayName: String,
    val symbol: String,
    val currentPrice: Double?, // null 허용
    val change: Double?,       // null 허용
    val isValid: Boolean       // 데이터 유효 여부
)


data class EtfSpec(
    val symbol: String,
    val displayName: String
)

val MAJOR_ETFS = listOf(
    EtfSpec("DIA", "Dow Jones"),
    EtfSpec("SPY", "S&P 500"),
    EtfSpec("QQQ", "Nasdaq"),
    EtfSpec("IWM", "Small Cap"),
    EtfSpec("VXX", "Market Volatility")
)