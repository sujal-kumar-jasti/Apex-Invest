package com.apexinvest.app.ui.screens

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexinvest.app.ui.components.AppTickerText
import com.apexinvest.app.ui.components.CommonScreenHeader
import com.apexinvest.app.ui.components.glassCard
import com.apexinvest.app.ui.theme.BrandPurple
import com.apexinvest.app.ui.theme.LocalAppColors
import com.apexinvest.app.util.getCurrencySymbol
import com.apexinvest.app.util.toCleanString
import com.apexinvest.app.viewmodel.PortfolioViewModel
import com.apexinvest.app.viewmodel.TransactionUiModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

// Static Shapes
private val LedgerCardShape = RoundedCornerShape(32.dp)
private val RowContainerShape = RoundedCornerShape(20.dp)
private val PillShape = RoundedCornerShape(8.dp)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TransactionHistory(
    portfolioViewModel: PortfolioViewModel,
    onBack: () -> Unit,
    isConnected: Boolean
) {
    LaunchedEffect(Unit) {
        Log.d("TransactionHistory", "🚀 SCREEN_OPENED")
    }

    BackHandler { onBack() }

    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val appColors = LocalAppColors.current

    var isSyncing by rememberSaveable { mutableStateOf(false) }
    val selectedIds = rememberSaveable(
        saver = listSaver(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) { mutableStateListOf<Int>() }
    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }

    val isSelectionMode by remember { derivedStateOf { selectedIds.isNotEmpty() } }
    val uiState by portfolioViewModel.uiState.collectAsStateWithLifecycle()
    val analyticsState by portfolioViewModel.transactionAnalyticsState.collectAsStateWithLifecycle()

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val currencySymbol = remember(uiState.isUsd) { getCurrencySymbol(if (uiState.isUsd) "USD" else "INR") }

    val meshBrush = remember(isDark) {
        Brush.verticalGradient(listOf(BrandPurple.copy(alpha = if (isDark) 0.12f else 0.05f), Color.Transparent))
    }

    var canRenderHeavyList by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (!canRenderHeavyList) {
            delay(300)
            canRenderHeavyList = true
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Delete Transactions?", fontWeight = FontWeight.Bold) },
            text = { Text("Permanently remove ${selectedIds.size} records from your history? This will not affect your portfolio holdings.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            portfolioViewModel.deleteSelectedTransactions(selectedIds.toSet())
                            selectedIds.clear()
                            showConfirmDialog = false
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = appColors.trendRed)
                ) { Text("DELETE", fontWeight = FontWeight.Black) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("CANCEL") }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(meshBrush)) {
        CommonScreenHeader(
            applyStatusBarsPadding = isConnected,
            leadingContent = {
                AnimatedContent(targetState = isSelectionMode, label = "HeaderMode") { selecting ->
                    if (selecting) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { selectedIds.clear() }) {
                                Icon(Icons.Default.Close, "Cancel")
                            }
                            Text(
                                text = "${selectedIds.size} Selected",
                                fontWeight = FontWeight.ExtraBold,
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), CircleShape)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = if (isDark) Color.White else Color.Black)
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text("Global Ledger", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                                Text("Market Activity", style = MaterialTheme.typography.labelSmall, color = BrandPurple)
                            }
                        }
                    }
                }
            },
            trailingContent = {
                if (isSelectionMode) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = {
                            if (selectedIds.size == analyticsState.mappedHistory.size) {
                                selectedIds.clear()
                            } else {
                                selectedIds.clear()
                                selectedIds.addAll(analyticsState.mappedHistory.map { it.id })
                            }
                        }) {
                            Text(if (selectedIds.size == analyticsState.mappedHistory.size) "None" else "All", fontWeight = FontWeight.Bold)
                        }
                        IconButton(onClick = { showConfirmDialog = true }) {
                            Icon(Icons.Default.Delete, "Delete", tint = appColors.trendRed)
                        }
                    }
                } else {
                    IconButton(
                        onClick = {
                            scope.launch {
                                isSyncing = true
                                portfolioViewModel.loadPortfolioAndPrices()
                                isSyncing = false
                            }
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), CircleShape)
                    ) {
                        if (isSyncing) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = BrandPurple)
                        else Icon(Icons.Default.Refresh, "Sync", tint = BrandPurple, modifier = Modifier.size(20.dp))
                    }
                }
            }
        )

        // 🚀 3. RENDER PURE UI MODELS
        if (!canRenderHeavyList || analyticsState.isInitial) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = BrandPurple, strokeWidth = 3.dp, modifier = Modifier.size(36.dp))
            }
        } else if (analyticsState.mappedHistory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No market activity.", color = Color.Gray, fontWeight = FontWeight.Medium)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(top = 8.dp, bottom = 120.dp)
            ) {
                if (!isSelectionMode) {
                    item(key = "dashboard_summary", contentType = "summary") {
                        LedgerGlassSummaryCard(
                            totalBuy = analyticsState.totalBuy,
                            totalSell = analyticsState.totalSell,
                            currencySymbol = currencySymbol,
                            isDark = isDark
                        )
                        Spacer(Modifier.height(16.dp))
                    }
                }

                item(key = "stream_header", contentType = "header") {
                    Text(
                        text = "Order Stream",
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(
                    items = analyticsState.mappedHistory,
                    key = { it.id },
                    contentType = { "transaction_row" }
                ) { item ->
                    val isSelected = remember(selectedIds.size) { selectedIds.contains(item.id) }
                    
                    SideEffect {
                        Log.d("TransactionHistory", "📦 DRAW_ITEM: ${item.symbol}")
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 5.dp)
                    ) {
                        CommonTransactionRow(
                            item = item,
                            currencySymbol = currencySymbol,
                            isSelected = isSelected,
                            isSelectionMode = isSelectionMode,
                            onClick = remember(item.id, isSelectionMode) {
                                {
                                    Log.d("TransactionHistory", "👆 CLICK: ${item.symbol}")
                                    if (isSelectionMode) {
                                        if (selectedIds.contains(item.id)) selectedIds.remove(item.id)
                                        else selectedIds.add(item.id)
                                    }
                                }
                            },
                            onLongClick = remember(item.id, isSelectionMode) {
                                {
                                    Log.d("TransactionHistory", "👆 LONG_CLICK: ${item.symbol}")
                                    if (!isSelectionMode) {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        selectedIds.add(item.id)
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LedgerGlassSummaryCard(totalBuy: Double, totalSell: Double, currencySymbol: String, isDark: Boolean) {
    val appColors = LocalAppColors.current
    val netFlow = totalBuy - totalSell
    val isNetPositive = netFlow >= 0
    val totalVolume = totalBuy + totalSell
    val buyRatio = if (totalVolume > 0) (totalBuy / totalVolume).toFloat() else 0.5f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .shadow(20.dp, LedgerCardShape, spotColor = BrandPurple.copy(alpha = 0.15f))
            .glassCard(isDark, LedgerCardShape)
    ) {
        Column(modifier = Modifier.padding(22.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(42.dp).background(BrandPurple.copy(alpha = 0.12f), CircleShape), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AccountBalanceWallet, null, tint = BrandPurple, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("NET CASH FLOW", fontSize = 9.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                        Text(if (isNetPositive) "Capital Flow In" else "Capital Flow Out", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                AppTickerText(
                    value = abs(netFlow),
                    currencySymbol = if (isNetPositive) "+$currencySymbol" else "-$currencySymbol",
                    textStyle = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Black, color = if (isNetPositive) appColors.trendGreen else MaterialTheme.colorScheme.onSurface)
                )
            }

            Spacer(Modifier.height(18.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MiniFlowPill("Invested", "$currencySymbol${totalBuy.toCleanString()}", appColors.trendGreen, Modifier.weight(1f))
                MiniFlowPill("Liquidated", "$currencySymbol${totalSell.toCleanString()}", appColors.trendRed, Modifier.weight(1f))
            }

            Spacer(Modifier.height(18.dp))
            DistributionBar(buyRatio = buyRatio)
        }
    }
}

@Composable
private fun MiniFlowPill(title: String, value: String, color: Color, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).background(color, CircleShape))
                Spacer(Modifier.width(6.dp))
                Text(title, style = MaterialTheme.typography.labelSmall, color = Color.Gray, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Black, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CommonTransactionRow(
    item: TransactionUiModel, // 🚀 Fully mapped by the ViewModel
    currencySymbol: String,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val appColors = LocalAppColors.current
    val trendColor = if (item.isBuy) appColors.trendGreen else appColors.trendRed
    val icon = if (item.isBuy) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown

    Surface(
        shape = RowContainerShape,
        color = if (isSelected) BrandPurple.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, if (isSelected) BrandPurple else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.1f)),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RowContainerShape)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(visible = isSelectionMode, enter = expandHorizontally() + fadeIn(), exit = shrinkHorizontally()) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    modifier = Modifier.padding(end = 12.dp),
                    colors = CheckboxDefaults.colors(checkedColor = BrandPurple)
                )
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(trendColor.copy(alpha = 0.12f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = trendColor, modifier = Modifier.size(20.dp))
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.symbol, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(trendColor.copy(alpha = 0.15f), PillShape)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(if (item.isBuy) "BUY" else "SELL", fontSize = 9.sp, fontWeight = FontWeight.Black, color = trendColor)
                    }
                }
                Spacer(Modifier.height(3.dp))
                Text("${item.quantityStr} Units  •  ${item.formattedDate}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (item.isBuy) "+" else "-"}$currencySymbol${item.totalValue.toCleanString()}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Black,
                    color = trendColor
                )
                Text("@ $currencySymbol${item.convertedPrice.toCleanString()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun DistributionBar(buyRatio: Float) {
    val appColors = LocalAppColors.current
    val animatedRatio by animateFloatAsState(targetValue = buyRatio, animationSpec = tween(800), label = "RatioAnim")
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth().height(10.dp).clip(CircleShape).background(Color.Gray.copy(alpha = 0.1f))) {
            Box(Modifier.weight(animatedRatio.coerceIn(0.01f, 0.99f)).fillMaxHeight().background(appColors.trendGreen))
            Box(Modifier.weight((1f - animatedRatio).coerceIn(0.01f, 0.99f)).fillMaxHeight().background(appColors.trendRed))
        }
        Row(Modifier.fillMaxWidth().padding(top = 6.dp), Arrangement.SpaceBetween) {
            Text("Buy ${(buyRatio * 100).toInt()}%", color = appColors.trendGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text("Sell ${((1f - buyRatio) * 100).toInt()}%", color = appColors.trendRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
    }
}