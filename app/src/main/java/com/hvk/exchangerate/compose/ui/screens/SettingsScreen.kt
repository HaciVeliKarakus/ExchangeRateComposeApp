package com.hvk.exchangerate.compose.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    refreshInterval: Int,
    isDarkTheme: Boolean,
    onRefreshIntervalChange: (Int) -> Unit,
    onDarkThemeChange: (Boolean) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Yenileme Aralığı Ayarı
            Card {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Yenileme Aralığı",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "$refreshInterval dakika",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilledTonalButton(
                                onClick = { 
                                    if (refreshInterval > 1) {
                                        onRefreshIntervalChange(refreshInterval - 1)
                                    }
                                }
                            ) {
                                Text("-")
                            }
                            FilledTonalButton(
                                onClick = { onRefreshIntervalChange(refreshInterval + 1) }
                            ) {
                                Text("+")
                            }
                        }
                    }
                }
            }
            
            // Karanlık Tema Ayarı
            Card {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Karanlık Tema",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = onDarkThemeChange
                    )
                }
            }
        }
    }
} 