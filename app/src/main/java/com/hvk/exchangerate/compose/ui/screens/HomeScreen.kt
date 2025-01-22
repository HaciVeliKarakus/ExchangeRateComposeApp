package com.hvk.exchangerate.compose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.hvk.exchangerate.compose.worker.ExchangeRateWorker
import androidx.datastore.preferences.core.Preferences
import androidx.glance.currentState
import com.hvk.exchangerate.compose.widget.ExchangeRateWidget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    euroRate: String?,
    usdRate: String?,
    gbpRate: String?,
    lastUpdate: String?,
    isLoading: Boolean,
    onRefresh: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Döviz Kurları") },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Ayarlar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Euro Card
            RateCard(
                currencyCode = "EUR/TRY",
                rate = euroRate ?: "-",
                color = Color(0xFF4CAF50)
            )
            
            // USD Card
            RateCard(
                currencyCode = "USD/TRY",
                rate = usdRate ?: "-",
                color = Color(0xFF64B5F6)
            )
            
            // GBP Card
            RateCard(
                currencyCode = "GBP/TRY",
                rate = gbpRate ?: "-",
                color = Color(0xFFFF9800)
            )
            
            Spacer(modifier = Modifier.weight(1f))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Son Güncelleme: ${lastUpdate ?: "-"}",
                    color = Color.Gray,
                    fontSize = 12.sp
                )
                
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Yenile")
                    }
                }
            }
        }
    }
}

@Composable
fun RateCard(
    currencyCode: String,
    rate: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF2C2C2C))
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = currencyCode,
                color = color,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$rate ₺",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
} 