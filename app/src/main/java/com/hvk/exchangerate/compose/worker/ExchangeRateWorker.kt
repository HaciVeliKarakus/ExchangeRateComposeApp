package com.hvk.exchangerate.compose.worker

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import com.hvk.exchangerate.compose.data.ExchangeRateApi
import com.hvk.exchangerate.compose.widget.ExchangeRateWidget
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.hvk.exchangerate.compose.data.ApiConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ExchangeRateWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val api = Retrofit.Builder()
        .baseUrl(ApiConfig.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ExchangeRateApi::class.java)

    override suspend fun doWork(): Result {
        return try {
            // USD bazında TRY, EUR ve GBP kurlarını al
            val response = api.getLatestRates()
            
            // Kurları al
            val usdToTry = response.rates["TRY"] ?: 1.0
            val usdToEur = response.rates["EUR"] ?: 1.0
            val usdToGbp = response.rates["GBP"] ?: 1.0
            
            // Çapraz kurları hesapla
            val eurToTry = usdToTry / usdToEur
            val gbpToTry = usdToTry / usdToGbp

            // Şu anki zamanı formatla
            val currentTime = SimpleDateFormat("HH:mm", Locale("tr")).format(Date())
            
            // Widget'ları güncelle
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(ExchangeRateWidget::class.java)
            val widget = ExchangeRateWidget()
            
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs: MutablePreferences ->
                    prefs[ExchangeRateWidget.euroRateKey] = eurToTry.toString()
                    prefs[ExchangeRateWidget.usdRateKey] = usdToTry.toString()
                    prefs[ExchangeRateWidget.gbpRateKey] = gbpToTry.toString()
                    prefs[ExchangeRateWidget.lastUpdateKey] = currentTime
                }
                widget.update(context, glanceId)
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        private const val WORK_NAME = "ExchangeRateUpdate"

        fun startPeriodicWork(context: Context) {
            val request = PeriodicWorkRequestBuilder<ExchangeRateWorker>(
                10, TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }
    }
} 