package com.hvk.exchangerate.compose.worker

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.datastore.preferences.core.MutablePreferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequest
import com.hvk.exchangerate.compose.data.ExchangeRateApi
import com.hvk.exchangerate.compose.receiver.WidgetUpdateReceiver
import com.hvk.exchangerate.compose.widget.ExchangeRateWidget
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class ExchangeRateWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val api = ExchangeRateApi()

    override suspend fun doWork(): Result {
        // Güncelleme başladığında toast mesaj göster
        showToast("Döviz kurları güncelleniyor...")
        
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
                    prefs[ExchangeRateWidget.isLoadingKey] = false
                }
                widget.update(context, glanceId)
            }
            
            val intent = Intent(WidgetUpdateReceiver.ACTION_UPDATE_WIDGET).apply {
                setPackage(context.packageName)
            }
            context.sendBroadcast(intent)
            
            // Güncelleme başarılı olduğunda toast mesaj göster
            showToast("Döviz kurları güncellendi")
            
            Result.success()
        } catch (e: Exception) {
            // Hata durumunda da yükleme durumunu false yap
            val manager = GlanceAppWidgetManager(context)
            val glanceIds = manager.getGlanceIds(ExchangeRateWidget::class.java)
            val widget = ExchangeRateWidget()
            
            glanceIds.forEach { glanceId ->
                updateAppWidgetState(context, glanceId) { prefs: MutablePreferences ->
                    prefs[ExchangeRateWidget.isLoadingKey] = false
                }
                widget.update(context, glanceId)
            }
            
            // Güncelleme yayını yap
            val intent = Intent(WidgetUpdateReceiver.ACTION_UPDATE_WIDGET).apply {
                setPackage(context.packageName)
            }
            context.sendBroadcast(intent)
            
            // Hata durumunda toast mesaj göster
            showToast("Döviz kurları güncellenemedi")
            
            Result.retry()
        }
    }
    
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    companion object {
        private const val WORK_NAME = "ExchangeRateUpdate"

        fun startPeriodicWork(context: Context, intervalMinutes: Int = 10) {
            val request = PeriodicWorkRequestBuilder<ExchangeRateWorker>(
                intervalMinutes.toLong(), TimeUnit.MINUTES
            ).build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
            
            // Yenileme aralığı değiştiğinde toast mesaj göster
            Toast.makeText(
                context,
                "Döviz kurları ${intervalMinutes} dakikada bir güncellenecek",
                Toast.LENGTH_SHORT
            ).show()
        }

        fun startOneTimeWork(context: Context) {
            val request = OneTimeWorkRequest.Builder(ExchangeRateWorker::class.java)
                .build()
            
            WorkManager.getInstance(context).enqueue(request)
        }
    }
} 