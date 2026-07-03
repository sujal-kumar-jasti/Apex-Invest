package com.apexinvest.app.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.apexinvest.app.viewmodel.AiUiState
import com.apexinvest.app.viewmodel.InsightType
import com.apexinvest.app.viewmodel.PortfolioViewModel
import com.apexinvest.app.viewmodel.StockSuggestion
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds


private const val GlassAlphaDark = 0.9f
private const val GlassAlphaLight = 0.4f
private val FocusButtonShape = RoundedCornerShape(30.dp)
private val CardShape = RoundedCornerShape(24.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIInvestmentIdeasScreen(
    portfolioViewModel: PortfolioViewModel,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    isBackEnabled: Boolean = false,
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
        }
    }
}

@Composable
fun ErrorView(message: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
            Spacer(Modifier.height(16.dp))
            Text(message, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
        }
    }
}