package com.example.a3dmodelsample.retrofit

import com.example.a3dmodelsample.retrofit.data.EtfQuoteUi
import com.example.a3dmodelsample.retrofit.data.MAJOR_ETFS

class EtfRepository(
    private val api: AlphaVantageApiService,
    private val apiKey: String
) {

    suspend fun fetchMajorEtfQuotes(): List<EtfQuoteUi> {
        return MAJOR_ETFS.map { etf ->
            val response = runCatching {
                api.globalQuote(symbol = etf.symbol,  apiKey = apiKey)
            }.getOrNull()

            val quote = response?.globalQuote
            val price = quote?.price?.toDoubleOrNull()
            val change = quote?.change?.toDoubleOrNull()

            if (price != null && change != null) {
                // normal data
                EtfQuoteUi(
                    displayName = etf.displayName,
                    symbol = etf.symbol,
                    currentPrice = price,
                    change = change,
                    isValid = true
                )
            } else {
                EtfQuoteUi(
                    displayName = etf.displayName,
                    symbol = etf.symbol,
                    currentPrice = null,
                    change = null,
                    isValid = false
                )
            }
        }
    }

}
