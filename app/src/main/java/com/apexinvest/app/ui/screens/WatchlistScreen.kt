package com.apexinvest.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexinvest.app.ui.components.CommonScreenHeader
import com.apexinvest.app.ui.components.CommonSearchOverlay
import com.apexinvest.app.ui.components.CommonSearchResultRow
import com.apexinvest.app.ui.components.CommonStockRow
import com.apexinvest.app.ui.navigation.Screen
import com.apexinvest.app.ui.theme.BrandPurple
import com.apexinvest.app.util.getConvertedValue
import com.apexinvest.app.util.guessCurrencyFromSymbol
import com.apexinvest.app.viewmodel.PortfolioViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchlistScreen(
    portfolioViewModel: PortfolioViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    isConnected: Boolean
) {
    var showSearchOverlay by remember { mutableStateOf(false) }

    BackHandler {
        if (showSearchOverlay) showSearchOverlay = false else onBack()
    }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val uiState by portfolioViewModel.uiState.collectAsStateWithLifecycle()
    val watchlist by portfolioViewModel.watchlistStocks.collectAsStateWithLifecycle()

    var isManualRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val pullRefreshState = rememberPullToRefreshState()

    val meshBrush = remember(isDark) {
        Brush.verticalGradient(
            listOf(BrandPurple.copy(alpha = if (isDark) 0.12f else 0.05f), Color.Transparent)
        )
    }

    LaunchedEffect(isConnected) {
        if (isConnected && watchlist.isEmpty()) {
            portfolioViewModel.loadPortfolioAndPrices()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(meshBrush)
    ) {
        CommonScreenHeader(
            title = "Watchlist",
            onBackClick = onBack,
            applyStatusBarsPadding = isConnected,
            trailingContent = {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(BrandPurple.copy(alpha = 0.15f), CircleShape)
                        .border(1.dp, BrandPurple.copy(alpha = 0.3f), CircleShape)
                        .clip(CircleShape)
                        .clickable { showSearchOverlay = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Add, "Add Stock", tint = BrandPurple, modifier = Modifier.size(24.dp))
                }
            }
        )

        PullToRefreshBox(
            isRefreshing = isManualRefreshing,
            onRefresh = {
                scope.launch {
                    isManualRefreshing = true
                    portfolioViewModel.loadPortfolioAndPrices()
                    delay(500)
                    isManualRefreshing = false
                }
            },
            state = pullRefreshState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            indicator = {
                PullToRefreshDefaults.Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = isManualRefreshing,
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                    color = BrandPurple,
                    state = pullRefreshState
                )
            }
        ) {
            if (watchlist.isEmpty() && !uiState.isLoading) {
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(36.dp))
                        Spacer(Modifier.height(12.dp))
                        Text("Your watchlist is empty.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(watchlist, key = { it.symbol }) { stock ->
                        // 🚀 OPTIMIZATION: Memoize stable properties and expensive conversions.
                        val passCurrency = remember(stock.symbol) { guessCurrencyFromSymbol(stock.symbol) }
                        val convertedPrice = remember(stock.lastPrice, uiState.isUsd, uiState.rates) {
                            getConvertedValue(stock.lastPrice, stock.symbol, uiState.isUsd, uiState.rates)
                        }
                        val historyData = remember(stock.symbol, isManualRefreshing) {
                            portfolioViewModel.getCachedSparkline(stock.symbol)
                        }

                        Box(Modifier.animateItem()) {
                            CommonStockRow(
                                symbol = stock.symbol,
                                companyName = stock.companyName,
                                price = convertedPrice,
                                percentChange = stock.changePercent,
                                isUsd = uiState.isUsd,
                                historyData = historyData,
                                isDark = isDark,
                                onDelete = { portfolioViewModel.deleteWatchlistStock(stock.symbol) }
                            ) {
                                onNavigate(Screen.StockDetail.createRoute(stock.symbol, passCurrency))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSearchOverlay) {
        var searchQuery by remember { mutableStateOf("") }
        val searchResults by portfolioViewModel.searchResults.collectAsState()
        val focusManager = LocalFocusManager.current

        CommonSearchOverlay(
            query = searchQuery,
            onQueryChange = {
                searchQuery = it
                if (it.length >= 2) portfolioViewModel.searchStocks(it) else portfolioViewModel.clearSearchResults()
            },
            onDismiss = { showSearchOverlay = false },
            isDark = isDark,
            placeholder = "Search tickers to add..."
        ) {
            if (searchQuery.isEmpty()) {
                Box(Modifier.align(Alignment.Center)) {
                    Text("Find a global asset to track.", color = Color.Gray, fontSize = 15.sp)
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(searchResults, key = { it.symbol }) { stock ->
                        CommonSearchResultRow(
                            symbol = stock.symbol,
                            name = stock.name,
                            exchange = stock.exchange
                        ) {
                            focusManager.clearFocus()
                            portfolioViewModel.addWatchlistStock(stock.symbol)
                            showSearchOverlay = false
                        }
                    }
                }
            }
        }
    }
}
