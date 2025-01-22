package com.hvk.exchangerate.compose.data

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.serialization.gson.*

class ExchangeRateApi {
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            gson()
        }
        install(Logging) {
            level = LogLevel.BODY
        }
    }

    suspend fun getLatestRates(
        from: String = "USD",
        to: String = "TRY,EUR,GBP"
    ): ExchangeRateResponse {
        return client.get("${ApiConfig.BASE_URL}latest") {
            url {
                parameters.append("from", from)
                parameters.append("to", to)
            }
        }.body()
    }
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