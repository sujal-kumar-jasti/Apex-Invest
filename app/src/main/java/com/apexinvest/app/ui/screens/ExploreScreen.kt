package com.apexinvest.app.ui.screens

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.apexinvest.app.data.model.SearchResultDto
import com.apexinvest.app.data.model.TrendingStockDto
import com.apexinvest.app.ui.navigation.Screen
import com.apexinvest.app.viewmodel.CommodityUiModel
import com.apexinvest.app.viewmodel.ExploreUiState
import com.apexinvest.app.viewmodel.ExploreViewModel
import com.apexinvest.app.viewmodel.SearchUiState

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onNavigate: (String) -> Unit,
    isConnected: Boolean
) {
    val uiState by viewModel.uiState.collectAsState()

    var cachedData by remember { mutableStateOf<ExploreUiState.Success?>(null) }
    var showSearchPopup by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is ExploreUiState.Success) {
            cachedData = uiState as ExploreUiState.Success
        }
    }

    LaunchedEffect(Unit) {
        if (cachedData == null) {
            viewModel.loadMarketData()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        // --- LAYOUT FIX 1: TOP BAR ---
        // Moved up by reducing top padding here
        ExploreTopBar(onSearchClick = { showSearchPopup = true })

        // --- LAYOUT FIX 2: GAP ---
        // Added explicit spacer to separate search bar from the list
        Spacer(modifier = Modifier.height(16.dp))

        // OFFLINE BANNER
        if (!isConnected) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFD32F2F))
                    .padding(vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Offline Mode - Showing Cached Prices",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        val dataToShow = (uiState as? ExploreUiState.Success) ?: cachedData

        if (dataToShow != null) {
            ExploreContent(
                data = dataToShow,
                onNavigate = onNavigate
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                when (uiState) {
                    is ExploreUiState.Loading -> CircularProgressIndicator()
                    is ExploreUiState.Error -> OfflineStateView { viewModel.loadMarketData() }
                    else -> {}
                }
            }
        }
    }

    if (showSearchPopup) {
        SearchPopup(
            viewModel = viewModel,
            onDismiss = { showSearchPopup = false },
            onNavigate = { route ->
                showSearchPopup = false
                onNavigate(route)
            }
        )
    }
}

@Composable
fun ExploreContent(
    data: ExploreUiState.Success,
    onNavigate: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(bottom = 80.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        if (data.indices.isNotEmpty()) {
            item {
                SectionHeader("Indian Indices")
                IndicesRow(data.indices, onNavigate)
                Spacer(Modifier.height(24.dp))
            }
        }

        if (data.trendingStocks.isNotEmpty()) {
            item {
                SectionHeader("Top Gainers (Nifty 50)", Icons.Default.TrendingUp)
                TrendingStocksRow(data.trendingStocks) { symbol ->
                    onNavigate(Screen.StockDetail.createRoute(symbol))
                }
                Spacer(Modifier.height(24.dp))
            }
        }

        if (data.commodities.isNotEmpty()) {
            item {
                SectionHeader("Commodities")
                IndicesRow(data.commodities, onNavigate)
                Spacer(Modifier.height(24.dp))
            }
        }

        if (data.globalIndices.isNotEmpty()) {
            item {
                SectionHeader("Global Markets", Icons.Default.Public)
                IndicesRow(data.globalIndices, onNavigate)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun SearchPopup(
    viewModel: ExploreViewModel,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val query by viewModel.searchQuery.collectAsState()
    val searchState by viewModel.searchUiState.collectAsState()

    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    LaunchedEffect(Unit) {
        try { focusRequester.requestFocus() } catch (_: Exception) {}
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                }

                TextField(
                    value = query,
                    onValueChange = { viewModel.onSearchQueryChanged(it) },
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    placeholder = { Text("Search stocks...") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = { viewModel.clearSearch() }) {
                                Icon(Icons.Default.Clear, null, tint = Color.Gray)
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                )
            }

            HorizontalDivider()

            Box(modifier = Modifier.fillMaxSize()) {
                when (val state = searchState) {
                    is SearchUiState.Idle -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Type to search...", color = Color.Gray) }
                    is SearchUiState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                    is SearchUiState.Empty -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No results found", color = Color.Gray) }
                    is SearchUiState.Success -> {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(
                                items = state.results,
                                key = { it.symbol ?: it.hashCode() }
                            ) { stock ->
                                SearchResultItem(stock) { onNavigate(Screen.StockDetail.createRoute(stock.symbol)) }
                            }
                        }
                    }
                    is SearchUiState.Error -> {
                        if (query.isNotEmpty()) {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text("Search failed", color = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(stock: SearchResultDto, onClick: () -> Unit) {
    val safeSymbol = stock.symbol.orEmpty()
    val safeName = stock.name.orEmpty()
    val safeExchange = stock.exchange.orEmpty()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            val initial = if (safeSymbol.isNotEmpty()) safeSymbol.take(1) else "?"
            Text(initial, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(safeSymbol, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(safeName, style = MaterialTheme.typography.bodySmall, color = Color.Gray, maxLines = 1)
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(4.dp)
        ) {
            Text(safeExchange, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun SectionHeader(title: String, icon: ImageVector? = null) {
    Row(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        if (icon != null) {
            Spacer(Modifier.width(8.dp))
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun IndicesRow(indices: List<CommodityUiModel>, onNavigate: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = indices,
            key = { it.symbol }
        ) { item ->
            IndexCard(item) { onNavigate(Screen.StockDetail.createRoute(item.symbol)) }
        }
    }
}

@Composable
fun IndexCard(item: CommodityUiModel, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(150.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(item.name, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            Text(item.value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            val color = if (item.isPositive) Color(0xFF00C853) else Color(0xFFD32F2F)
            Text(text = item.changePercent, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TrendingStocksRow(stocks: List<TrendingStockDto>, onClick: (String) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = stocks,
            key = { it.symbol }
        ) { stock ->
            TrendingStockCard(stock, onClick)
        }
    }
}

@Composable
fun TrendingStockCard(stock: TrendingStockDto, onClick: (String) -> Unit) {
    Card(
        onClick = { onClick(stock.symbol) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.width(160.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                val char = if (stock.symbol.isNotEmpty()) stock.symbol.take(1) else "?"
                Text(char, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer, fontSize = 16.sp)
            }
            Spacer(Modifier.height(12.dp))
            Text(stock.symbol, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(4.dp))
            Text("₹${stock.price}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(4.dp))
            val isPositive = stock.changePercent >= 0
            val color = if (isPositive) Color(0xFF00C853) else Color(0xFFD32F2F)
            val sign = if (stock.changePercent > 0) "+" else ""
            Text(text = "$sign${stock.changePercent}%", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun OfflineStateView(onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.WifiOff, null, tint = Color.Gray, modifier = Modifier.size(48.dp))
        Spacer(Modifier.height(16.dp))
        Text("No Internet Connection", fontWeight = FontWeight.Bold)
        Text("Check your settings.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Retry") }
    }
}

@Composable
fun ExploreTopBar(onSearchClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            // --- FIX 1: Reduced top padding here to 8.dp to move it "up" ---
            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 0.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .clickable { onSearchClick() }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Text("Search stocks, indices...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 16.sp)
    }
}