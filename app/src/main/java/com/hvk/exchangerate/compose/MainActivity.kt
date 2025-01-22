package com.hvk.exchangerate.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.hvk.exchangerate.compose.data.SettingsDataStore
import com.hvk.exchangerate.compose.receiver.WidgetUpdateReceiver
import com.hvk.exchangerate.compose.ui.screens.HomeScreen
import com.hvk.exchangerate.compose.ui.screens.SettingsScreen
import com.hvk.exchangerate.compose.widget.ExchangeRateWidget
import com.hvk.exchangerate.compose.worker.ExchangeRateWorker
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var settingsDataStore: SettingsDataStore
    private lateinit var widgetUpdateReceiver: WidgetUpdateReceiver
    
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { false }
        
        super.onCreate(savedInstanceState)
        
        settingsDataStore = SettingsDataStore(this)
        
        setContent {
            val navController = rememberNavController()
            val scope = rememberCoroutineScope()
            
            // Ayarları oku
            val isDarkTheme by settingsDataStore.isDarkTheme.collectAsState(initial = isSystemInDarkTheme())
            val refreshInterval by settingsDataStore.refreshInterval.collectAsState(initial = SettingsDataStore.DEFAULT_REFRESH_INTERVAL)
            
            // Widget verilerini oku
            var euroRate by remember { mutableStateOf<String?>(null) }
            var usdRate by remember { mutableStateOf<String?>(null) }
            var gbpRate by remember { mutableStateOf<String?>(null) }
            var lastUpdate by remember { mutableStateOf<String?>(null) }
            var isLoading by remember { mutableStateOf(false) }

            // Widget verilerini güncelle
            LaunchedEffect(Unit) {
                try {
                    val manager = GlanceAppWidgetManager(this@MainActivity)
                    val glanceIds = manager.getGlanceIds(ExchangeRateWidget::class.java)
                    if (glanceIds.isNotEmpty()) {
                        val state = getAppWidgetState(this@MainActivity, glanceIds.first())
                        euroRate = state[ExchangeRateWidget.euroRateKey]
                        usdRate = state[ExchangeRateWidget.usdRateKey]
                        gbpRate = state[ExchangeRateWidget.gbpRateKey]
                        lastUpdate = state[ExchangeRateWidget.lastUpdateKey]
                        isLoading = state[ExchangeRateWidget.isLoadingKey] ?: false
                    }
                } catch (e: Exception) {
                    // Widget henüz oluşturulmamış olabilir
                }
            }

            // Widget update receiver'ı oluştur
            DisposableEffect(Unit) {
                widgetUpdateReceiver = WidgetUpdateReceiver { newEuroRate, newUsdRate, newGbpRate, newLastUpdate, newIsLoading ->
                    euroRate = newEuroRate
                    usdRate = newUsdRate
                    gbpRate = newGbpRate
                    lastUpdate = newLastUpdate
                    isLoading = newIsLoading
                }
                WidgetUpdateReceiver.register(this@MainActivity, widgetUpdateReceiver)
                
                onDispose {
                    WidgetUpdateReceiver.unregister(this@MainActivity, widgetUpdateReceiver)
                }
            }
            
            MaterialTheme(
                colorScheme = if (isDarkTheme) darkColorScheme(
                    primary = Color(0xFF4CAF50),
                    secondary = Color(0xFF64B5F6),
                    tertiary = Color(0xFFFF9800),
                    background = Color(0xFF1E1E1E),
                    surface = Color(0xFF2C2C2C)
                ) else lightColorScheme(
                    primary = Color(0xFF4CAF50),
                    secondary = Color(0xFF64B5F6),
                    tertiary = Color(0xFFFF9800)
                )
            ) {
                Surface {
                    NavHost(navController = navController, startDestination = "home") {
                        composable("home") {
                            HomeScreen(
                                onNavigateToSettings = { navController.navigate("settings") },
                                euroRate = euroRate,
                                usdRate = usdRate,
                                gbpRate = gbpRate,
                                lastUpdate = lastUpdate,
                                isLoading = isLoading,
                                onRefresh = {
                                    scope.launch {
                                        isLoading = true
                                        ExchangeRateWorker.startOneTimeWork(this@MainActivity)
                                    }
                                }
                            )
                        }
                        composable("settings") {
                            SettingsScreen(
                                onNavigateBack = { navController.popBackStack() },
                                refreshInterval = refreshInterval,
                                isDarkTheme = isDarkTheme,
                                onRefreshIntervalChange = { newInterval ->
                                    scope.launch {
                                        settingsDataStore.setRefreshInterval(newInterval)
                                        ExchangeRateWorker.startPeriodicWork(this@MainActivity, newInterval)
                                    }
                                },
                                onDarkThemeChange = { enabled ->
                                    scope.launch {
                                        settingsDataStore.setDarkTheme(enabled)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
        
        // Widget güncelleme işini başlat
        ExchangeRateWorker.startPeriodicWork(this)
    }
}