package com.hvk.exchangerate.compose.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.hvk.exchangerate.compose.widget.ExchangeRateWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class WidgetUpdateReceiver(
    private val onUpdate: (
        euroRate: String?,
        usdRate: String?,
        gbpRate: String?,
        lastUpdate: String?,
        isLoading: Boolean
    ) -> Unit
) : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_UPDATE_WIDGET) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val manager = GlanceAppWidgetManager(context)
                    val glanceIds = manager.getGlanceIds(ExchangeRateWidget::class.java)
                    if (glanceIds.isNotEmpty()) {
                        val prefs = getAppWidgetState<Preferences>(
                            context = context,
                            definition = PreferencesGlanceStateDefinition,
                            glanceId = glanceIds.first()
                        )
                        onUpdate(
                            prefs[ExchangeRateWidget.euroRateKey],
                            prefs[ExchangeRateWidget.usdRateKey],
                            prefs[ExchangeRateWidget.gbpRateKey],
                            prefs[ExchangeRateWidget.lastUpdateKey],
                            prefs[ExchangeRateWidget.isLoadingKey] ?: false
                        )
                    }
                } catch (e: Exception) {
                    // Widget henüz oluşturulmamış olabilir
                }
            }
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.hvk.exchangerate.compose.WIDGET_UPDATE"

        fun register(context: Context, receiver: WidgetUpdateReceiver) {
            val filter = IntentFilter(ACTION_UPDATE_WIDGET)
            ContextCompat.registerReceiver(
                context,
                receiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        fun unregister(context: Context, receiver: WidgetUpdateReceiver) {
            context.unregisterReceiver(receiver)
        }
    }
}