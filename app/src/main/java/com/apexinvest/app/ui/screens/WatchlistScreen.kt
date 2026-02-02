package com.apexinvest.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apexinvest.app.data.WatchlistEntity
import com.apexinvest.app.ui.components.StockSearchField
import com.apexinvest.app.ui.navigation.Screen
import com.apexinvest.app.util.getConvertedValue
import com.apexinvest.app.util.toCleanString
import com.apexinvest.app.viewmodel.PortfolioViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    portfolioViewModel: PortfolioViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    isConnected: Boolean
) {
    // 1. Observe Unified State (Fixes Sync & Flickering)
    val state by portfolioViewModel.uiState.collectAsState()

    // 2. Control Dialog via ViewModel (Allows opening from Shortcuts/MainActivity)
    val showAddDialog by portfolioViewModel.showAddWatchlistDialog.collectAsState()

    Scaffold(
        // 1. FIX: Ensure Scaffold matches your app's background color
        containerColor = MaterialTheme.colorScheme.background,

        // 2. FIX: Handle Content Padding (Body)
        contentWindowInsets = if (!isConnected) {
            WindowInsets(0.dp) // Offline: Banner pushes body down -> 0 padding
        } else {
            ScaffoldDefaults.contentWindowInsets // Online: Normal padding
        },

        // 3. FIX: Move Header to TopAppBar (Handles Status Bar Automatically)
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Watchlist",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    // Optional: Add Back button if needed, otherwise empty
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    // A. Currency Toggle
                    TextButton(onClick = { portfolioViewModel.toggleCurrency() }) {
                        Text(
                            if (state.isUsd) "USD" else "INR",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // B. Refresh Button
                    IconButton(onClick = { portfolioViewModel.loadPortfolioAndPrices() }) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Sync, "Refresh")
                        }
                    }

                    // C. Add Button
                    IconButton(
                        onClick = { portfolioViewModel.showAddWatchlistDialog.value = true },
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .size(36.dp) // Explicit size for better touch target
                    ) {
                        Icon(
                            Icons.Default.Add,
                            "Add",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background, // Match background
                    scrolledContainerColor = MaterialTheme.colorScheme.background
                ),
                // 4. FIX: Handle Header Padding (Top Bar itself)
                windowInsets = if (!isConnected) {
                    WindowInsets(0.dp) // Offline: Header sits directly under banner
                } else {
                    TopAppBarDefaults.windowInsets // Online: Header sits under status bar
                }
            )
        }
    ) { innerPadding ->

        // --- BODY CONTENT ---
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding) // Apply the smart padding from Scaffold
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Note: Custom Header Row is REMOVED (Moved to TopAppBar above)

            // --- LIST ---
            if (state.watchlist.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Watchlist is empty", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(items = state.watchlist, key = { it.symbol }) { stock ->
                        WatchlistItem(
                            stock = stock,
                            isUsd = state.isUsd,
                            liveRate = state.liveRate,
                            onDelete = { portfolioViewModel.deleteWatchlistStock(stock.symbol) },
                            onClick = { onNavigate(Screen.StockDetail.createRoute(stock.symbol)) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { portfolioViewModel.showAddWatchlistDialog.value = false },
                title = { Text("Track Stock") },
                text = {
                    Column {
                        Text("Search Symbol", style = MaterialTheme.typography.bodySmall)
                        StockSearchField(portfolioViewModel) { symbol ->
                            portfolioViewModel.addWatchlistStock(symbol)
                            portfolioViewModel.showAddWatchlistDialog.value = false
                        }
                    }
                },
                confirmButton = {}
            )
        }
    }
}

@Composable
fun WatchlistItem(
    stock: WatchlistEntity,
    isUsd: Boolean,
    liveRate: Double,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val currency = if (isUsd) "$" else "₹"
    val price = getConvertedValue(stock.lastPrice, stock.symbol, isUsd, liveRate)

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.5f)),
        elevation = CardDefaults.cardElevation(0.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(stock.symbol, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Last Traded Price", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$currency${price.toCleanString()}",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (stock.lastPrice > 0) Color(0xFF4CAF50) else Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.width(16.dp))
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}