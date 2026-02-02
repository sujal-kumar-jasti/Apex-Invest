package com.apexinvest.app.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.apexinvest.app.data.StockEntity
import com.apexinvest.app.ui.components.TradeEntrySheet
import com.apexinvest.app.ui.navigation.Screen
import com.apexinvest.app.util.getConvertedValue
import com.apexinvest.app.util.toCleanString
import com.apexinvest.app.viewmodel.PortfolioViewModel
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PortfolioScreen(
    portfolioViewModel: PortfolioViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    initialStockToBuy: String? = null, // New Parameter for AI redirection
    isConnected: Boolean
) {
    val state by portfolioViewModel.uiState.collectAsState()
    val searchResults by portfolioViewModel.searchResults.collectAsState()

    // 1. Logic to handle Auto-Opening the Trade Sheet
    var searchQuery by remember { mutableStateOf(initialStockToBuy ?: "") }
    var showTradeSheet by remember { mutableStateOf(initialStockToBuy != null) }

    val user = FirebaseAuth.getInstance().currentUser
    val context = LocalContext.current

    val onLinkDematClick: () -> Unit = {
        user?.uid?.let { uid ->
            val backendUrl = "https://jsujalkumar7899-prognosai-fastapi-backend-1.hf.space/api/v1/aa/login?uid=$uid"
            try {
                CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(context, Uri.parse(backendUrl))
            } catch (e: Exception) {
                Toast.makeText(context, "Browser not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(

        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    portfolioViewModel.clearSearchResults()
                    searchQuery = "" // Reset query for manual add
                    showTradeSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, "Add Trade") },
                text = { Text("Add Trade", fontWeight = FontWeight.Bold) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
                .background(MaterialTheme.colorScheme.background)
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "My Portfolio",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Currency Toggle
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .clickable { portfolioViewModel.toggleCurrency() }
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = if (state.isUsd) "USD" else "INR",
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Sync
                    IconButton(onClick = { portfolioViewModel.loadPortfolioAndPrices() }) {
                        if (state.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Sync, "Sync")
                        }
                    }

                    // Broker Link
                    IconButton(onClick = onLinkDematClick) {
                        Icon(Icons.Default.AccountBalance, "Link Demat", tint = MaterialTheme.colorScheme.secondary)
                    }
                }
            }

            // --- SUMMARY CARD ---
            if (state.portfolio.isNotEmpty()) {
                PortfolioSummaryHeader(state.portfolio, state.isUsd, state.liveRate)
            }

            // --- HOLDINGS LIST ---
            if (state.portfolio.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Your portfolio is empty.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = state.portfolio, key = { it.symbol }) { stock ->
                        PortfolioItemCardClean(
                            stock = stock,
                            isUsd = state.isUsd,
                            liveRate = state.liveRate,
                            onDelete = {
                                portfolioViewModel.executeTrade(stock.symbol, false, stock.quantity.toString(), stock.currentPrice.toString(), LocalDate.now().toString())
                            },
                            onClick = { onNavigate(Screen.StockDetail.createRoute(stock.symbol)) }
                        )
                    }
                    item { Spacer(Modifier.height(80.dp)) }
                }
            }
        }

        // --- TRADE SHEET ---
        if (showTradeSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showTradeSheet = false
                    searchQuery = "" // Reset on close
                },
                containerColor = MaterialTheme.colorScheme.surface,
                sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
            ) {
                TradeEntrySheet(
                    onDismiss = { showTradeSheet = false },
                    portfolioStocks = state.portfolio.map { it.symbol },
                    searchResults = searchResults,
                    // Pass the query (empty for manual, pre-filled for AI redirect)
                    initialQuery = searchQuery,
                    onSearch = { query ->
                        searchQuery = query
                        portfolioViewModel.searchStocks(query)
                    },
                    onConfirm = { symbol, isBuy, qty, price, date ->
                        portfolioViewModel.executeTrade(symbol, isBuy, qty, price, date)
                        showTradeSheet = false
                    }
                )
            }
        }
    }
}

@Composable
fun PortfolioSummaryHeader(portfolio: List<StockEntity>, isUsd: Boolean, liveRate: Double) {
    val currentVal = portfolio.sumOf { getConvertedValue(it.currentPrice * it.quantity, it.symbol, isUsd, liveRate) }
    val investedVal = portfolio.sumOf { getConvertedValue(it.buyPrice * it.quantity, it.symbol, isUsd, liveRate) }
    val totalGain = currentVal - investedVal
    val totalGainPct = if (investedVal > 0) (totalGain / investedVal) * 100 else 0.0
    val currency = if (isUsd) "$" else "₹"
    val isProfit = totalGain >= 0
    val color = if (isProfit) Color(0xFF00C853) else Color(0xFFD32F2F)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Current Value", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text("$currency${currentVal.toCleanString()}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Total Returns", style = MaterialTheme.typography.labelMedium, color = Color.Gray)
                Text(
                    "${if (isProfit) "+" else ""}${currency}${totalGain.toCleanString()} (${totalGainPct.toCleanString()}%)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }
    }
}

// --- NEW "CLEAN & PRO" CARD DESIGN ---
@Composable
fun PortfolioItemCardClean(
    stock: StockEntity,
    isUsd: Boolean,
    liveRate: Double,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val currency = if (isUsd) "$" else "₹"

    // Values
    val totalValue = getConvertedValue(stock.currentPrice * stock.quantity, stock.symbol, isUsd, liveRate)
    val avgPrice = getConvertedValue(stock.buyPrice, stock.symbol, isUsd, liveRate)
    val ltp = getConvertedValue(stock.currentPrice, stock.symbol, isUsd, liveRate)

    // Profit Logic
    val invested = stock.buyPrice * stock.quantity
    val currentRaw = stock.currentPrice * stock.quantity
    val gainRaw = currentRaw - invested
    val gainPct = if (invested > 0) (gainRaw / invested) * 100 else 0.0
    val isProfit = gainPct >= 0
    val color = if (isProfit) Color(0xFF00C853) else Color(0xFFD32F2F)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // --- ICON ---
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stock.symbol.take(1),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(Modifier.width(16.dp))

            // --- DATA GRID ---
            Column(modifier = Modifier.weight(1f)) {
                // ROW 1: Symbol & Total Value
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stock.symbol,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$currency${totalValue.toCleanString()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(4.dp))

                // ROW 2: Qty/Avg & LTP/Percent
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${stock.quantity} @ $currency${avgPrice.toCleanString()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$currency${ltp.toCleanString()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "(${if (isProfit) "+" else ""}${gainPct.toCleanString()}%)",
                            style = MaterialTheme.typography.bodySmall,
                            color = color,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            // --- DELETE BUTTON ---
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sell All",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}