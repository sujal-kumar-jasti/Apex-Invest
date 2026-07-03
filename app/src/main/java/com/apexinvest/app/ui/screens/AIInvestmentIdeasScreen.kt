package com.apexinvest.app.ui.screens

<<<<<<< HEAD
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
=======
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
<<<<<<< HEAD
import androidx.compose.foundation.layout.Spacer
=======
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
<<<<<<< HEAD
=======
import androidx.compose.foundation.layout.statusBarsPadding
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
<<<<<<< HEAD
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
=======
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
<<<<<<< HEAD
import androidx.compose.material3.OutlinedTextFieldDefaults
=======
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
<<<<<<< HEAD
import androidx.compose.runtime.SideEffect
=======
import androidx.compose.runtime.collectAsState
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
<<<<<<< HEAD
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexinvest.app.ui.components.CommonScreenHeader
import com.apexinvest.app.ui.components.InsightCard
import com.apexinvest.app.ui.components.TradeEntrySheet
import com.apexinvest.app.ui.components.glassCard
import com.apexinvest.app.ui.navigation.Screen
import com.apexinvest.app.ui.theme.BrandPurple
import com.apexinvest.app.ui.theme.LocalAppColors
import com.apexinvest.app.util.guessCurrencyFromSymbol
=======
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apexinvest.app.ui.components.TradeEntrySheet
import com.apexinvest.app.ui.navigation.Screen
import com.apexinvest.app.viewmodel.AiInsight
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import com.apexinvest.app.viewmodel.AiUiState
import com.apexinvest.app.viewmodel.InsightType
import com.apexinvest.app.viewmodel.PortfolioViewModel
import com.apexinvest.app.viewmodel.StockSuggestion
import kotlinx.coroutines.delay
<<<<<<< HEAD
import kotlin.time.Duration.Companion.milliseconds


private const val GlassAlphaDark = 0.9f
private const val GlassAlphaLight = 0.4f
private val FocusButtonShape = RoundedCornerShape(30.dp)
private val CardShape = RoundedCornerShape(24.dp)
=======
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIInvestmentIdeasScreen(
    portfolioViewModel: PortfolioViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    isBackEnabled: Boolean = false,
<<<<<<< HEAD
    isConnected: Boolean,
) {
    val aiState by portfolioViewModel.aiState.collectAsStateWithLifecycle()
    val thematicState by portfolioViewModel.thematicState.collectAsStateWithLifecycle()
    val searchResults by portfolioViewModel.searchResults.collectAsStateWithLifecycle()
    val portfolioState by portfolioViewModel.uiState.collectAsStateWithLifecycle()

    var selectedFocus by remember { mutableStateOf("Portfolio") }
    var themeInput by remember { mutableStateOf("") }
    var showTradeSheet by remember { mutableStateOf(value = false) }
    var stockToBuy by remember { mutableStateOf("") }

    // 🌟 DEFERRED RENDERING: Stops the heavy UI components from breaking the slide animation
    var canRenderHeavyContent by rememberSaveable{ mutableStateOf(false) }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val isDark = remember(surfaceColor) { surfaceColor.luminance() < 0.5f }

    val meshBrush = remember(isDark) {
        Brush.verticalGradient(
            listOf(BrandPurple.copy(alpha = if (isDark) 0.12f else 0.05f), Color.Transparent)
        )
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    LaunchedEffect(Unit) {
        if (!canRenderHeavyContent) {
            delay(350) // Only pay the transition tax once!
            canRenderHeavyContent = true
        }
        if (portfolioViewModel.aiState.value is AiUiState.Idle) {
            portfolioViewModel.generateIdeas()
        }
    }
    LaunchedEffect(isConnected) {
        if (isConnected) {
            portfolioViewModel.autoHealAI(lastThemeSearched = themeInput)
        }
    }

    if (isBackEnabled) {
        BackHandler { onBack() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(meshBrush)
    ) {
        // Renders instantly to give the illusion of a fast load
        CommonScreenHeader(
            onBackClick = if (isBackEnabled) onBack else null,
            applyStatusBarsPadding = isConnected,
            leadingContent = {
                Column {
                    Text("Apex Invest Advisor", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text("Powered by AI Analysis", style = MaterialTheme.typography.labelSmall, color = BrandPurple, fontWeight = FontWeight.Bold)
                }
            }
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            GlassyFocusButton(
                label = "Portfolio Audit",
                isSelected = selectedFocus == "Portfolio",
                onClick = { if (selectedFocus != "Portfolio") selectedFocus = "Portfolio" },
                modifier = Modifier.weight(1f),
                isDark = isDark
            )
            GlassyFocusButton(
                label = "Thematic Finder",
                isSelected = selectedFocus == "Thematic",
                onClick = { if (selectedFocus != "Thematic") selectedFocus = "Thematic" },
                modifier = Modifier.weight(1f),
                isDark = isDark
            )
        }

        Spacer(Modifier.height(12.dp))

        Box(modifier = Modifier.weight(1f)) {
            // Only draw the heavy AnimatedContent and Lists if the slide animation is completely finished
            if (canRenderHeavyContent) {
                AnimatedContent(
                    targetState = selectedFocus,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(200)) },
                    label = "TabSwitch"
                ) { target ->
                    if (target == "Portfolio") {
                        AiResultsList(
                            state = aiState,
                            introText = "I have analyzed your holdings. Here is your personalized strategy.",
                            isDark = isDark,
                            onNavigate = onNavigate
                        ) { symbol -> stockToBuy = symbol; showTradeSheet = true }
                    } else {
                        Column {
                            OutlinedTextField(
                                value = themeInput,
                                onValueChange = { themeInput = it },
                                placeholder = { Text("Search Theme (e.g., Green Tech, AI)", color = Color.Gray) },
                                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                                shape = FocusButtonShape,
                                trailingIcon = {
                                    IconButton(
                                        onClick = { portfolioViewModel.generateIdeas(theme = themeInput) },
                                        enabled = themeInput.isNotBlank(),
                                        modifier = Modifier.background(BrandPurple, CircleShape).size(36.dp)
                                    ) {
                                        Icon(Icons.Default.Search, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    }
                                },
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(if(isDark) GlassAlphaDark else GlassAlphaLight),
                                    focusedContainerColor = MaterialTheme.colorScheme.surface.copy(if(isDark) GlassAlphaDark else GlassAlphaLight),
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f),
                                    focusedBorderColor = BrandPurple
                                )
                            )
                            Spacer(Modifier.height(12.dp))
                            AiResultsList(
                                state = thematicState,
                                introText = "Generating high-conviction picks for '$themeInput'...",
                                isDark = isDark,
                                onNavigate = onNavigate
                            ) { symbol -> stockToBuy = symbol; showTradeSheet = true }
                        }
                    }
                }
            } else {
                // Blank space preserves layout height during animation without blocking UI thread
                Spacer(Modifier.fillMaxSize())
            }
        }

        if (showTradeSheet) {
            ModalBottomSheet(
                onDismissRequest = { showTradeSheet = false },
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
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

@Composable
fun AiResultsList(state: AiUiState, introText: String, isDark: Boolean, onNavigate: (String) -> Unit, onBuy: (String) -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        when (state) {
            is AiUiState.Thinking -> PremiumThinkingAnimation()
            is AiUiState.Error -> ErrorView(state.message)
            is AiUiState.Success -> {
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 140.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    item { TypewriterText(introText, isDark) }

                    items(state.insights) { insight ->
                        val appColors = LocalAppColors.current
                        val (color, icon) = when (insight.type) {
                            InsightType.WARNING -> appColors.trendRed to Icons.Default.Warning
                            InsightType.OPPORTUNITY -> Color(0xFF448AFF) to Icons.AutoMirrored.Filled.TrendingUp
                            InsightType.SUCCESS -> appColors.trendGreen to Icons.Default.CheckCircle
                        }
                        InsightCard(title = insight.title, description = insight.description, icon = icon, color = color, isDark = isDark)
                    }

                    if (state.suggestions.isNotEmpty()) {
                        item {
                            Text(
                                "Top Recommendations",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
                                color = if(isDark) Color.White else Color.Black
                            )
                        }
                        items(state.suggestions) { stock ->
                            GlassySuggestionCard(
                                stock = stock,
                                isDark = isDark,
                                onDetailsClick = {
                                    val curr = guessCurrencyFromSymbol(stock.symbol)
                                    onNavigate(Screen.StockDetail.createRoute(stock.symbol, curr))
                                },
                                onBuyClick = { onBuy(stock.symbol) }
                            )
                        }
                    }
                }
            }
            is AiUiState.Idle -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.AutoAwesome, null, tint = BrandPurple.copy(alpha = 0.5f), modifier = Modifier.size(56.dp))
                    Spacer(Modifier.height(16.dp))
                    Text("Enter a theme to unlock AI insights", color = Color.Gray, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
fun PremiumThinkingAnimation() {
    val transition = rememberInfiniteTransition(label = "thinking")
    val alpha by transition.animateFloat(
        initialValue = 0.3f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "alpha"
    )
    Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.size(80.dp),
                strokeWidth = 2.dp,
                color = BrandPurple.copy(alpha = 0.2f)
            )
            Icon(Icons.Default.AutoAwesome, null, tint = BrandPurple, modifier = Modifier.size(32.dp).alpha(alpha))
        }
        Spacer(Modifier.height(24.dp))
        Text("Analyzing Market Structure...", fontWeight = FontWeight.Bold, color = BrandPurple)
    }
}

@Composable
fun GlassyFocusButton(label: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier, isDark: Boolean) {
    val containerColor = if (isSelected) BrandPurple else MaterialTheme.colorScheme.surface.copy(alpha = if(isDark) GlassAlphaDark else GlassAlphaLight)
    val contentColor = if (isSelected) Color.White else Color.White

    Box(
        modifier = modifier
            .height(48.dp)
            .shadow(if (isSelected) 8.dp else 0.dp, FocusButtonShape, spotColor = BrandPurple.copy(alpha = 0.3f))
            .clip(FocusButtonShape)
            .background(containerColor)
            .clickable { onClick() }
            .border(1.dp, if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), FocusButtonShape),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = contentColor, fontWeight = FontWeight.Black, fontSize = 13.sp)
    }
}

@Composable
fun TypewriterText(text: String, isDark: Boolean) {
    var displayedText by remember { mutableStateOf("") }
    LaunchedEffect(text) {
        displayedText = ""
        text.forEachIndexed { idx, _ ->
            displayedText = text.take(idx + 1)
            delay(15.milliseconds)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(BrandPurple.copy(alpha = 0.08f))
            .border(1.dp, BrandPurple.copy(alpha = 0.15f), CardShape)
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.Top) {
            Icon(Icons.Default.AutoAwesome, null, tint = BrandPurple, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(16.dp))
            Text(displayedText, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, lineHeight = 26.sp, color = if(isDark) Color.White else Color.Black)
        }
    }
}

@Composable
fun GlassySuggestionCard(stock: StockSuggestion, isDark: Boolean, onDetailsClick: () -> Unit, onBuyClick: () -> Unit) {
    val initial = remember(stock.symbol) { stock.symbol.take(1) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(isDark, CardShape)
            .clickable { onDetailsClick() }
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(BrandPurple.copy(alpha = 0.1f), RoundedCornerShape(18.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initial,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.titleLarge,
                    color = BrandPurple
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = stock.symbol,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if(isDark) Color.White else Color.Black
                )
                Text(
                    text = stock.sector,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = stock.reason,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = if(isDark) Color.White.copy(alpha = 0.7f) else Color.Black.copy(alpha = 0.7f)
                )
            }
            IconButton(
                onClick = onBuyClick,
                modifier = Modifier.background(BrandPurple, CircleShape).size(40.dp)
            ) { Icon(Icons.Default.Add, null, tint = Color.White, modifier = Modifier.size(22.dp)) }
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
        }
    }
}

<<<<<<< HEAD
@Composable
fun ErrorView(message: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text(message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
        }
    }
}