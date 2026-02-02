package com.apexinvest.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apexinvest.app.ui.components.TradeEntrySheet
import com.apexinvest.app.ui.navigation.Screen
import com.apexinvest.app.viewmodel.AiInsight
import com.apexinvest.app.viewmodel.AiUiState
import com.apexinvest.app.viewmodel.InsightType
import com.apexinvest.app.viewmodel.PortfolioViewModel
import com.apexinvest.app.viewmodel.StockSuggestion
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIInvestmentIdeasScreen(
    portfolioViewModel: PortfolioViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    isBackEnabled: Boolean = false,
    isConnected: Boolean
) {
    // 1. Collect States
    val aiState by portfolioViewModel.aiState.collectAsState()
    val thematicState by portfolioViewModel.thematicState.collectAsState()
    val searchResults by portfolioViewModel.searchResults.collectAsState()
    val portfolioState by portfolioViewModel.uiState.collectAsState()

    // 2. Local State
    var selectedFocus by remember { mutableStateOf("Portfolio") }
    var themeInput by remember { mutableStateOf("") }

    // 3. Trade Sheet State
    var showTradeSheet by remember { mutableStateOf(false) }
    var stockToBuy by remember { mutableStateOf("") }

    // Auto-trigger Portfolio Analysis on entry if idle
    LaunchedEffect(Unit) {
        if (aiState is AiUiState.Idle) {
            portfolioViewModel.generateIdeas(null)
        }
    }

    // Handle Back Button only if enabled
    if (isBackEnabled) {
        BackHandler { onBack() }
    }
    Scaffold(
        contentWindowInsets = if (!isConnected) {
            WindowInsets(0.dp) // Offline: Banner pushes us down -> 0 padding
        } else {
            ScaffoldDefaults.contentWindowInsets // Online: Normal status bar padding
        }
    ) { innerPadding ->

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .statusBarsPadding()
            ) {
                // --- HEADER ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hide Back Button if not enabled
                    if (isBackEnabled) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                        Spacer(Modifier.width(8.dp))
                    }

                    Text(
                        text = "AI Strategist",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                // --- TABS ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FocusButton(
                        label = "Portfolio Audit",
                        isSelected = selectedFocus == "Portfolio",
                        onClick = { selectedFocus = "Portfolio" }
                    )
                    FocusButton(
                        label = "Thematic Finder",
                        isSelected = selectedFocus == "Thematic",
                        onClick = { selectedFocus = "Thematic" }
                    )
                }

                Spacer(Modifier.height(8.dp))

                // --- MAIN CONTENT AREA ---
                if (selectedFocus == "Portfolio") {
                    // TAB 1: PORTFOLIO
                    AiResultsList(
                        state = aiState,
                        introText = "I have analyzed your holdings. Here is your personalized strategy.",
                        onNavigate = onNavigate,
                        onBuy = { symbol ->
                            stockToBuy = symbol
                            showTradeSheet = true
                        }
                    )
                } else {
                    // TAB 2: THEMATIC (Search Bar + List)
                    Column {
                        // Search Bar
                        OutlinedTextField(
                            value = themeInput,
                            onValueChange = { themeInput = it },
                            label = { Text("Enter Theme (e.g., EV, AI, Defence)") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                IconButton(
                                    onClick = { portfolioViewModel.generateIdeas(themeInput) },
                                    enabled = themeInput.isNotBlank()
                                ) {
                                    Icon(Icons.Default.Search, null)
                                }
                            }
                        )
                        Spacer(Modifier.height(8.dp))

                        // Results List
                        AiResultsList(
                            state = thematicState,
                            introText = "Here are the top picks and risks for the '$themeInput' theme.",
                            onNavigate = onNavigate,
                            onBuy = { symbol ->
                                stockToBuy = symbol
                                showTradeSheet = true
                            }
                        )
                    }
                }
            }

            // --- DIRECT TRADE SHEET ---
            if (showTradeSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showTradeSheet = false },
                    containerColor = MaterialTheme.colorScheme.surface,
                    sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ) {
                    TradeEntrySheet(
                        onDismiss = { showTradeSheet = false },
                        portfolioStocks = portfolioState.portfolio.map { it.symbol },
                        searchResults = searchResults,
                        initialQuery = stockToBuy,
                        onSearch = { query -> portfolioViewModel.searchStocks(query) },
                        onConfirm = { symbol, isBuy, qty, price, date ->
                            portfolioViewModel.executeTrade(symbol, isBuy, qty, price, date)
                            showTradeSheet = false
                        }
                    )
                }
            }
        }
    }
}

// ==========================================
// UNIFIED RESULTS LIST
// ==========================================
@Composable
fun AiResultsList(
    state: AiUiState,
    introText: String,
    onNavigate: (String) -> Unit,
    onBuy: (String) -> Unit
) {
    when (state) {
        is AiUiState.Thinking -> ThinkingAnimation("Analyzing Market Data...")
        is AiUiState.Error -> ErrorView(state.message)
        is AiUiState.Success -> {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    TypewriterText(introText)
                }

                // Insights (Risks/Opportunities)
                items(state.insights) { insight -> InsightCard(insight) }

                item {
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.width(8.dp))
                        Text("Top Picks", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }

                // Suggestions (Stock Cards)
                items(state.suggestions) { stock ->
                    SuggestionCard(
                        stock = stock,
                        onDetailsClick = { onNavigate(Screen.StockDetail.createRoute(stock.symbol)) },
                        onBuyClick = { onBuy(stock.symbol) } // Opens sheet directly
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
        is AiUiState.Idle -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Enter a theme above to generate insights.", color = Color.Gray)
            }
        }
    }
}

// ==========================================
// SHARED COMPONENTS
// ==========================================

@Composable
fun ThinkingAnimation(text: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary, strokeWidth = 4.dp)
        Spacer(Modifier.height(24.dp))
        Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ErrorView(message: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Error: $message", color = MaterialTheme.colorScheme.error)
    }
}

@Composable
fun RowScope.FocusButton(label: String, isSelected: Boolean, onClick: () -> Unit) {
    val containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    Card(
        onClick = onClick,
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Box(Modifier.fillMaxWidth().padding(vertical = 14.dp), contentAlignment = Alignment.Center) {
            Text(label, color = contentColor, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun TypewriterText(text: String) {
    val isAnimated = rememberSaveable { mutableStateOf(false) }
    var displayedText by remember { mutableStateOf(if (isAnimated.value) text else "") }

    LaunchedEffect(text) {
        if (!isAnimated.value) {
            text.forEachIndexed { index, _ ->
                displayedText = text.take(index + 1)
                delay(20)
            }
            isAnimated.value = true
        }
    }
    Text(displayedText, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
}

@Composable
fun InsightCard(insight: AiInsight) {
    // FIX: Distinct Colors based on Type
    val (color, icon) = when (insight.type) {
        InsightType.WARNING -> Color(0xFFD32F2F) to Icons.Default.Warning // RED
        InsightType.OPPORTUNITY -> Color(0xFF2962FF) to Icons.Default.TrendingUp // BLUE
        InsightType.SUCCESS -> Color(0xFF00C853) to Icons.Default.CheckCircle // GREEN
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Icon(icon, null, tint = color, modifier = Modifier.size(28.dp))
            Spacer(Modifier.width(16.dp))
            Column {
                Text(insight.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
                Spacer(Modifier.height(4.dp))
                Text(insight.description, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
fun SuggestionCard(stock: StockSuggestion, onDetailsClick: () -> Unit, onBuyClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onDetailsClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                        Text(stock.symbol.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(stock.symbol, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(stock.sector, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                }
                IconButton(onClick = onBuyClick, modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape).size(32.dp)) {
                    Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Top) {
                Icon(Icons.Default.AutoAwesome, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(16.dp).padding(top = 2.dp))
                Spacer(Modifier.width(8.dp))
                Text(stock.reason, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}