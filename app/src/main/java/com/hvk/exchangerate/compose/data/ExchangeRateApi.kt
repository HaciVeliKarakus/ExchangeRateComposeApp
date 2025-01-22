package com.hvk.exchangerate.compose.data

import retrofit2.http.GET
import retrofit2.http.Query

interface ExchangeRateApi {
    @GET("latest")
    suspend fun getLatestRates(
        @Query("from") from: String = "USD",
        @Query("to") to: String = "TRY,EUR,GBP"
    ): ExchangeRateResponse
}

data class ExchangeRateResponse(
    val amount: Double,
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)

object ApiConfig {
    const val BASE_URL = "https://api.frankfurter.app/"
} 