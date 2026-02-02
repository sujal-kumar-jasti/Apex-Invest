package com.apexinvest.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.apexinvest.app.data.StockEntity
import com.apexinvest.app.ui.components.PremiumLineChart
import com.apexinvest.app.ui.navigation.Screen
import com.apexinvest.app.util.getConvertedValue
import com.apexinvest.app.util.toCleanString
import com.apexinvest.app.viewmodel.PortfolioViewModel
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    portfolioViewModel: PortfolioViewModel,
    onNavigate: (String) -> Unit,
    isConnected: Boolean
) {
    val user = FirebaseAuth.getInstance().currentUser
    val state by portfolioViewModel.uiState.collectAsState()
    val notificationsEnabled by portfolioViewModel.notificationsEnabled.collectAsState()

    var isManualRefreshing by remember { mutableStateOf(false) }
    LaunchedEffect(state.isLoading) {
        if (!state.isLoading) {
            isManualRefreshing = false
        }
    }

    // This handles the "Immediate Update" when internet comes back or app first opens.
    var wasPreviouslyConnected by rememberSaveable { mutableStateOf(isConnected) }

    LaunchedEffect(isConnected) {
        if (isConnected) {
            val isRestoredConnection = !wasPreviouslyConnected // True only if we were offline before
            val isFirstLoad = state.portfolio.isEmpty() // True if app just opened

            if (isFirstLoad || isRestoredConnection) {
                // We do NOT set isManualRefreshing = true here, so this update is "Silent"
                portfolioViewModel.loadPortfolioAndPrices()
            }
        }
        // Update history for next time
        wasPreviouslyConnected = isConnected
    }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(30_000) // Wait 30 seconds

            // Only refresh if we have internet.
            // This is "Silent" because we don't touch isManualRefreshing.
            if (isConnected) {
                portfolioViewModel.loadPortfolioAndPrices()
            }
        }
    }

    // --- MEMOIZED CALCULATIONS ---
    val portfolioStats by remember(state.portfolio, state.isUsd, state.liveRate) {
        derivedStateOf {
            val totalVal = state.portfolio.sumOf {
                getConvertedValue(it.currentPrice * it.quantity, it.symbol, state.isUsd, state.liveRate)
            }
            val totalInv = state.portfolio.sumOf {
                getConvertedValue(it.buyPrice * it.quantity, it.symbol, state.isUsd, state.liveRate)
            }
            val gain = totalVal - totalInv
            val positive = gain >= 0
            val percent = if (totalInv > 0) (gain / totalInv) * 100 else 0.0

            val chart = if (totalVal > 0) listOf(totalVal * 0.9, totalVal * 0.95, totalVal) else emptyList()

            PortfolioStats(totalVal, totalInv, gain, percent, positive, chart)
        }
    }

    val currency = if (state.isUsd) "$" else "₹"
    val scrollState = rememberScrollState()

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    // --- PULL TO REFRESH (User can still force update manually) ---
    PullToRefreshBox(
        isRefreshing = isManualRefreshing,
        onRefresh = {
            isManualRefreshing = true
            portfolioViewModel.loadPortfolioAndPrices() },
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // --- 1. HEADER SECTION ---
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Profile Row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onNavigate(Screen.Profile.route) }
                            .padding(4.dp)
                    ) {
                        if (user?.photoUrl != null) {
                            AsyncImage(
                                model = user.photoUrl,
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .border(1.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Surface(
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    null,
                                    modifier = Modifier.padding(8.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(greeting, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text(
                                user?.displayName ?: "Investor",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Notification & Currency Actions
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Currency Toggle
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier
                                .clickable { portfolioViewModel.toggleCurrency() }
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                text = if (state.isUsd) "USD" else "INR",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Notification Icon
                        IconButton(onClick = { portfolioViewModel.toggleNotifications() }) {
                            Icon(
                                imageVector = if (notificationsEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                                contentDescription = "Toggle Notifications",
                                tint = if (notificationsEnabled) MaterialTheme.colorScheme.primary else Color.Gray
                            )
                        }
                    }
                }
            }

            // --- 2. PREMIUM PORTFOLIO CARD ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .height(200.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    )
                    .clickable { onNavigate(Screen.Analytics.route) }
            ) {
                if (portfolioStats.chartData.isNotEmpty()) {
                    PremiumLineChart(
                        dataPoints = portfolioStats.chartData,
                        isPositive = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 0.dp),
                        strokeColor = Color.White.copy(alpha = 0.3f),
                        fillColor = Color.White.copy(alpha = 0.1f)
                    )
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Total Net Worth",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Spacer(Modifier.height(4.dp))
                        TickerText(
                            value = portfolioStats.totalValue,
                            currencySymbol = currency,
                            textStyle = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.15f))
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        val pnlIcon = if (portfolioStats.isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown
                        val sign = if (portfolioStats.isPositive) "+" else ""

                        Icon(pnlIcon, null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "$sign$currency${portfolioStats.totalGain.toCleanString()}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            text = "($sign${String.format(Locale.US, "%.2f", portfolioStats.totalPercent)}%)",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // --- 3. QUICK ACTIONS GRID ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                QuickActionItem(Icons.Default.Visibility, "Watchlist", Color(0xFFE3F2FD), Color(0xFF1976D2)) { onNavigate(Screen.Watchlist.route) }
                QuickActionItem(Icons.Default.PieChart, "Analytics", Color(0xFFF3E5F5), Color(0xFF7B1FA2)) { onNavigate(Screen.Analytics.route) }
                QuickActionItem(Icons.Default.Analytics, "Predict", Color(0xFFE8F5E9), Color(0xFF388E3C)) { onNavigate(Screen.Predictions.route) }
                QuickActionItem(Icons.Default.Lightbulb, "AI Ideas", Color(0xFFFFF3E0), Color(0xFFF57C00)) { onNavigate(Screen.InvestmentIdeas.route) }
            }

            Spacer(Modifier.height(32.dp))

            // --- 4. TOP HOLDINGS ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Top Holdings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.clickable { onNavigate(Screen.Portfolio.route) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("View All", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(16.dp))

            if (state.portfolio.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(120.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PieChart, null, tint = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Text("No investments yet.", color = Color.Gray)
                        Text("Start building your portfolio.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
            } else {
                Column(Modifier.padding(horizontal = 20.dp)) {
                    val topStocks = remember(state.portfolio, state.isUsd, state.liveRate) {
                        state.portfolio.sortedByDescending { stock ->
                            val rawGain = (stock.currentPrice - stock.buyPrice) * stock.quantity
                            getConvertedValue(rawGain, stock.symbol, state.isUsd, state.liveRate)
                        }.take(5)
                    }
                    topStocks.forEach { stock ->
                        DashboardStockRow(stock, state.isUsd, state.liveRate) {
                            onNavigate(Screen.StockDetail.createRoute(stock.symbol))
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}

// ---------------- DATA & COMPONENTS ----------------

data class PortfolioStats(
    val totalValue: Double,
    val totalInvested: Double,
    val totalGain: Double,
    val totalPercent: Double,
    val isPositive: Boolean,
    val chartData: List<Double>
)

@Composable
fun QuickActionItem(icon: ImageVector, label: String, color: Color, iconColor: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(color),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun DashboardStockRow(stock: StockEntity, isUsd: Boolean, liveRate: Double, onClick: () -> Unit) {
    val currency = if (isUsd) "$" else "₹"
    val price = getConvertedValue(stock.currentPrice, stock.symbol, isUsd, liveRate)
    val isProfit = stock.dailyChange >= 0
    val changeColor = if (isProfit) Color(0xFF00C853) else Color(0xFFD32F2F)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(0.5.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stock.symbol.first().toString(), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.titleMedium)
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(stock.symbol, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${stock.quantity} shares", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("$currency${price.toCleanString()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "${if (isProfit) "+" else ""}${stock.dailyChange.toCleanString()}%", style = MaterialTheme.typography.bodySmall, color = changeColor, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun TickerText(
    value: Double,
    currencySymbol: String,
    textStyle: androidx.compose.ui.text.TextStyle,
    modifier: Modifier = Modifier
) {
    // We remember the previous value to decide direction (Up or Down)
    var previousValue by remember { mutableDoubleStateOf(value) }
    val direction = if (value > previousValue) AnimatedContentTransitionScope.SlideDirection.Up else AnimatedContentTransitionScope.SlideDirection.Down

    // Update previous value *after* recomposition logic
    SideEffect { previousValue = value }

    AnimatedContent(
        targetState = value,
        transitionSpec = {
            // Slide in from bottom if going up, slide in from top if going down
            (slideIntoContainer(direction) + fadeIn()).togetherWith(
                slideOutOfContainer(direction) + fadeOut()
            ).using(
                SizeTransform(clip = false)
            )
        },
        label = "TickerAnimation"
    ) { targetValue ->
        Text(
            text = "$currencySymbol${targetValue.toCleanString()}",
            style = textStyle,
            // Flash Green/Red logic can be added here, but for now we stick to white/styled
            color = textStyle.color,
            modifier = modifier
        )
    }
}