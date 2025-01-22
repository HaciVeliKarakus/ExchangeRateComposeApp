package com.hvk.exchangerate.compose.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.appwidget.cornerRadius
import androidx.glance.text.FontWeight
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.Image
import androidx.glance.currentState
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.action.clickable
import androidx.glance.ImageProvider
import androidx.glance.action.ActionParameters
import androidx.glance.layout.Alignment
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.hvk.exchangerate.compose.R
import com.hvk.exchangerate.compose.worker.ExchangeRateWorker

class ExchangeRateWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF1E1E1E))
                    .cornerRadius(16.dp)
                    .padding(16.dp)
            ) {
                Column {
                    // Euro kuru
                    currentState<Preferences>().get(euroRateKey)?.let { euroRate ->
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .background(Color(0xFF2C2C2C))
                                .cornerRadius(8.dp)
                                .padding(8.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "EUR/TRY",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF4CAF50)),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = GlanceModifier.width(8.dp))
                            Text(
                                text = "%.2f ₺".format(euroRate.toDouble()),
                                style = TextStyle(
                                    color = ColorProvider(Color.White),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(12.dp))
                    
                    // Dolar kuru
                    currentState<Preferences>().get(usdRateKey)?.let { usdRate ->
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .background(Color(0xFF2C2C2C))
                                .cornerRadius(8.dp)
                                .padding(8.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "USD/TRY",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF64B5F6)),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = GlanceModifier.width(8.dp))
                            Text(
                                text = "%.2f ₺".format(usdRate.toDouble()),
                                style = TextStyle(
                                    color = ColorProvider(Color.White),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(12.dp))
                    
                    // Sterlin kuru
                    currentState<Preferences>().get(gbpRateKey)?.let { gbpRate ->
                        Row(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .background(Color(0xFF2C2C2C))
                                .cornerRadius(8.dp)
                                .padding(8.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Text(
                                text = "GBP/TRY",
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFFFF9800)),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            Spacer(modifier = GlanceModifier.width(8.dp))
                            Text(
                                text = "%.2f ₺".format(gbpRate.toDouble()),
                                style = TextStyle(
                                    color = ColorProvider(Color.White),
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                    
                    Spacer(modifier = GlanceModifier.height(12.dp))
                    Text(
                        text = "Son Güncelleme: " + (currentState<Preferences>()[lastUpdateKey] ?: ""),
                        style = TextStyle(
                            color = ColorProvider(Color(0xFF9E9E9E)),
                            fontSize = 12.sp
                        )
                    )
                    Row {
                        Image(
                            provider = ImageProvider(R.drawable.ic_refresh),
                            contentDescription = "Yenile",
                            modifier = GlanceModifier
                                .size(24.dp)
                                .clickable(actionRunCallback<RefreshAction>())
                        )
                    }
                }
            }
        }
    }

    companion object {
        val euroRateKey = stringPreferencesKey("euro_rate")
        val usdRateKey = stringPreferencesKey("usd_rate")
        val gbpRateKey = stringPreferencesKey("gbp_rate")
        val lastUpdateKey = stringPreferencesKey("last_update")
    }
}

class RefreshAction : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val workRequest = OneTimeWorkRequestBuilder<ExchangeRateWorker>().build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }
}

class ExchangeRateWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ExchangeRateWidget()
} 