package com.apexinvest.app.ui.screens

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexinvest.app.data.model.TrendingStockDto
import com.apexinvest.app.ui.components.CommonSearchOverlay
import com.apexinvest.app.ui.components.CommonSearchResultRow
import com.apexinvest.app.ui.components.SectionHeader
import com.apexinvest.app.ui.navigation.Screen
import com.apexinvest.app.viewmodel.CommodityUiModel
import com.apexinvest.app.viewmodel.ExploreUiState
import com.apexinvest.app.viewmodel.ExploreViewModel
import com.apexinvest.app.viewmodel.SearchUiState
import kotlinx.coroutines.delay

private val ThemePurple = Color(0xFF673AB7)
private val ShimmerShape = RoundedCornerShape(4.dp)

// 🚀 STANDARDIZED SHIMMER EFFECT
fun Modifier.shimmerEffect(): Modifier = composed {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val shimmerColors = if (isDark) {
        listOf(Color.DarkGray.copy(alpha = 0.6f), Color.LightGray.copy(alpha = 0.2f), Color.DarkGray.copy(alpha = 0.6f))
    } else {
        listOf(Color.LightGray.copy(alpha = 0.6f), Color.White.copy(alpha = 0.8f), Color.LightGray.copy(alpha = 0.6f))
    }

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    this.background(
        brush = Brush.linearGradient(
            colors = shimmerColors,
            start = Offset.Zero,
            end = Offset(x = translateAnimation.value, y = translateAnimation.value)
        )
    )
}

data class ExploreThemeColors(
    val isDark: Boolean,
    val greenTrend: Color,
    val greenBg: Color,
    val redTrend: Color,
    val redBg: Color,
    val cardBackground: Brush,
    val textColor: Color,
    val labelColor: Color,
    val borderColor: Color
)

@Composable
fun getExploreThemeColors(): ExploreThemeColors {
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    return ExploreThemeColors(
        isDark = isDark,
        greenTrend = if (isDark) Color(0xFF00E676) else Color(0xFF008940),
        greenBg = if (isDark) Color(0xFF00E676).copy(alpha = 0.15f) else Color(0xFFE8F5E9),
        redTrend = if (isDark) Color(0xFFFF5252) else Color(0xFFC62828),
        redBg = if (isDark) Color(0xFFFF5252).copy(alpha = 0.15f) else Color(0xFFFFEBEE),
        cardBackground = if (isDark) Brush.verticalGradient(listOf(Color(0xFF1E1E1E).copy(alpha = 0.8f), Color.Black.copy(alpha = 0.6f)))
        else Brush.verticalGradient(listOf(Color(0xFFFFFFFF).copy(alpha = 0.9f), Color(0xFFF5F5F5).copy(alpha = 0.7f))),
        textColor = if (isDark) Color.White else Color.Black,
        labelColor = if (isDark) Color.White.copy(0.5f) else Color.Black.copy(0.5f),
        borderColor = if (isDark) Color.White.copy(0.1f) else Color.Black.copy(0.1f)
    )
}

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onNavigate: (String) -> Unit,
    isConnected: Boolean,
    isSearchActive: Boolean = false,
    onSearchActiveChange: (Boolean) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val searchState by viewModel.searchUiState.collectAsStateWithLifecycle()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    var canRenderHeavyList by rememberSaveable { mutableStateOf(false) }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    val meshBrush = remember(isDark) {
        if (isDark) Brush.verticalGradient(listOf(ThemePurple.copy(alpha = 0.12f), Color.Transparent))
        else Brush.verticalGradient(listOf(ThemePurple.copy(alpha = 0.05f), Color.Transparent))
    }

    LaunchedEffect(Unit) {
        if (!canRenderHeavyList) {
            delay(350)
            canRenderHeavyList = true
        }
    }

    LaunchedEffect(isSearchActive) {
        if (!isSearchActive) { focusManager.clearFocus(); keyboardController?.hide(); viewModel.onSearchQueryChanged("") }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner, isConnected) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && isConnected) {
                viewModel.startAutoRefresh()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.stopAutoRefresh()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopAutoRefresh()
        }
    }

    BackHandler(enabled = isSearchActive) { onSearchActiveChange(false) }

    // 🚀 ARCHITECTURAL FIX: Replaced Scaffold with a raw Box that conditionally applies statusBarsPadding
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(meshBrush)
            .then(if (isConnected) Modifier.statusBarsPadding() else Modifier)
    ) {
        if (canRenderHeavyList) {
            ExploreContent(uiState, isConnected, onNavigate)
        }

        if (isSearchActive) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onSearchActiveChange(false) })
        }

        // FLOATING SEARCH PILL: 10dp margin applies cleanly below clock OR offline banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, start = 20.dp, end = 20.dp)
                .height(54.dp)
                .shadow(elevation = if (isDark) 16.dp else 8.dp, shape = CircleShape, spotColor = ThemePurple.copy(0.2f))
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.98f), CircleShape)
                .border(1.2.dp, if (isDark) Color.White.copy(0.15f) else Color.Black.copy(0.1f), CircleShape)
                .clip(CircleShape)
                .clickable { onSearchActiveChange(true) },
            contentAlignment = Alignment.CenterStart
        ) {
            Row(modifier = Modifier.padding(horizontal = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Search, null, Modifier.size(20.dp), tint = ThemePurple)
                Spacer(Modifier.width(12.dp))
                Text("Search markets, stocks, indices...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 15.sp)
            }
        }

        if (isSearchActive) {
            CommonSearchOverlay(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChanged(it) },
                onDismiss = { onSearchActiveChange(false) },
                isDark = isDark,
                placeholder = "Search stocks..."
            ) {
                when (val state = searchState) {
                    is SearchUiState.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(state.results, key = { it.symbol }) { stock ->
                                CommonSearchResultRow(
                                    symbol = stock.symbol,
                                    name = stock.name,
                                    exchange = stock.exchange
                                ) {
                                    onSearchActiveChange(false)
                                    val nativeCurrency = com.apexinvest.app.util.guessCurrencyFromSymbol(stock.symbol)
                                    onNavigate(Screen.StockDetail.createRoute(stock.symbol, nativeCurrency))
                                }
                            }
                        }
                    }
                    is SearchUiState.Loading -> {
                        Box(Modifier.fillMaxWidth().padding(40.dp), Alignment.Center) {
                            CircularProgressIndicator(strokeWidth = 3.dp, modifier = Modifier.size(32.dp), color = ThemePurple)
                        }
                    }
                    is SearchUiState.Empty -> {
                        Box(Modifier.fillMaxWidth().padding(40.dp), Alignment.Center) {
                            Text("No results found", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
                        }
                    }
                    else -> {
                        Box(Modifier.fillMaxWidth().padding(40.dp), Alignment.Center) {
                            Text("Type a symbol or company name...", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ExploreContent(
    uiState: ExploreUiState,
    isConnected: Boolean,
    onNavigate: (String) -> Unit
) {
    val mainListState = rememberLazyListState()
    val themeColors = getExploreThemeColors()

    val successData = uiState as? ExploreUiState.Success
    val isLoading = (uiState is ExploreUiState.Loading) || ((successData == null) && (uiState !is ExploreUiState.Error))

    val indices = successData?.indices ?: emptyList()
    val trending = successData?.trendingStocks ?: emptyList()
    val global = successData?.globalIndices ?: emptyList()
    val commodities = successData?.commodities ?: emptyList()

    // 🚀 THE FIX: Hardcoded static top padding (10dp top gap + 54dp search + 14dp list gap = 78dp)
    LazyColumn(
        state = mainListState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 78.dp, bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item(key = "h_overview") { SectionHeader("Market Overview", Icons.AutoMirrored.Filled.ShowChart) }
        item(key = "r_overview") { IndexCardsRow(items = indices, expectedCount = 4, isLoading = isLoading, colors = themeColors, onNavigate = onNavigate) }

        item(key = "h_trending") { SectionHeader("Top Movers", Icons.AutoMirrored.Filled.TrendingUp) }
        item(key = "r_trending") { TrendingCardsRow(items = trending, expectedCount = 10, isLoading = isLoading, colors = themeColors, onNavigate = onNavigate) }

        item(key = "h_global") { SectionHeader("Global Pulse", Icons.Default.Public) }
        item(key = "r_global") { IndexCardsRow(items = global, expectedCount = 9, isLoading = isLoading, colors = themeColors, onNavigate = onNavigate) }

        item(key = "h_comm") { SectionHeader("Commodities", Icons.Default.Diamond) }
        item(key = "r_comm") { IndexCardsRow(items = commodities, expectedCount = 5, isLoading = isLoading, colors = themeColors, onNavigate = onNavigate) }
    }
}

@Composable
fun IndexCardsRow(items: List<CommodityUiModel>, expectedCount: Int, isLoading: Boolean, colors: ExploreThemeColors, onNavigate: (String) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(expectedCount) { index ->
            if (isLoading || index >= items.size) {
                ProIndexCardShimmer(colors)
            } else {
                val item = items[index]
                ProIndexCard(item, colors) {
                    onNavigate(Screen.StockDetail.createRoute(item.symbol, item.currency))
                }
            }
        }
    }
}

@Composable
fun TrendingCardsRow(items: List<TrendingStockDto>, expectedCount: Int, isLoading: Boolean, colors: ExploreThemeColors, onNavigate: (String) -> Unit) {
    LazyRow(
        modifier = Modifier.fillMaxWidth().height(120.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        items(expectedCount) { index ->
            if (isLoading || index >= items.size) {
                ProTrendingCardShimmer(colors)
            } else {
                val item = items[index]
                ProTrendingCard(item, colors) { symbol ->
                    onNavigate(Screen.StockDetail.createRoute(symbol, item.currency ?: "USD"))
                }
            }
        }
    }
}

@Composable
fun ProIndexCard(item: CommodityUiModel, colors: ExploreThemeColors, onClick: () -> Unit) {
    val color = remember(item.isPositive, colors) { if (item.isPositive) colors.greenTrend else colors.redTrend }
    val bgTrackColor = remember(item.isPositive, colors) { if (item.isPositive) colors.greenBg else colors.redBg }

    Box(
        modifier = Modifier
            .width(145.dp).fillMaxHeight()
            .shadow(if (colors.isDark) 8.dp else 2.dp, RoundedCornerShape(24.dp), spotColor = ThemePurple.copy(0.1f))
            .clip(RoundedCornerShape(24.dp))
            .background(colors.cardBackground)
            .border(1.dp, colors.borderColor, RoundedCornerShape(24.dp))
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(item.name, color = colors.labelColor, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Column {
                Text(item.value, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium, color = colors.textColor, letterSpacing = (-0.5).sp)
                Box(Modifier.padding(top = 4.dp).background(bgTrackColor, RoundedCornerShape(30.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(item.changePercent, color = color, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

@Composable
fun ProTrendingCard(stock: TrendingStockDto, colors: ExploreThemeColors, onClick: (String) -> Unit) {
    val isPos = remember(stock.changePercent) { (stock.changePercent ?: 0.0) >= 0 }
    val color = remember(isPos, colors) { if (isPos) colors.greenTrend else colors.redTrend }
    val bgTrackColor = remember(isPos, colors) { if (isPos) colors.greenBg else colors.redBg }
    val currencySym = remember(stock.currency) { com.apexinvest.app.util.getCurrencySymbol(stock.currency ?: "") }
    val formattedChange = remember(stock.changePercent, isPos) {
        "${if(isPos) "+" else ""}${String.format("%.2f", stock.changePercent)}%"
    }

    Box(
        modifier = Modifier
            .width(145.dp).fillMaxHeight()
            .shadow(if (colors.isDark) 8.dp else 2.dp, RoundedCornerShape(24.dp), spotColor = ThemePurple.copy(0.1f))
            .clip(RoundedCornerShape(24.dp))
            .background(colors.cardBackground)
            .border(1.dp, colors.borderColor, RoundedCornerShape(24.dp))
            .clickable { onClick(stock.symbol) }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stock.symbol,
                fontWeight = FontWeight.ExtraBold,
                style = MaterialTheme.typography.titleMedium,
                color = colors.textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Column {
                Text(
                    text = "$currencySym${stock.price}",
                    color = colors.textColor,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-0.5).sp,
                    maxLines = 1
                )
                Box(Modifier.padding(top = 4.dp).background(bgTrackColor, RoundedCornerShape(30.dp)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text(
                        text = formattedChange,
                        color = color,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
fun ProIndexCardShimmer(colors: ExploreThemeColors) {
    Box(
        modifier = Modifier
            .width(145.dp).fillMaxHeight()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.cardBackground)
            .border(1.dp, colors.borderColor, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(Modifier.width(60.dp).height(12.dp).clip(ShimmerShape).shimmerEffect())
            Column {
                Box(Modifier.width(80.dp).height(18.dp).clip(ShimmerShape).shimmerEffect())
                Spacer(Modifier.height(8.dp))
                Box(Modifier.width(40.dp).height(12.dp).clip(ShimmerShape).shimmerEffect())
            }
        }
    }
}

@Composable
fun ProTrendingCardShimmer(colors: ExploreThemeColors) {
    Box(
        modifier = Modifier
            .width(145.dp).fillMaxHeight()
            .clip(RoundedCornerShape(24.dp))
            .background(colors.cardBackground)
            .border(1.dp, colors.borderColor, RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(Modifier.width(50.dp).height(16.dp).clip(ShimmerShape).shimmerEffect())
            Column {
                Box(Modifier.width(70.dp).height(18.dp).clip(ShimmerShape).shimmerEffect())
                Spacer(Modifier.height(8.dp))
                Box(Modifier.width(40.dp).height(12.dp).clip(ShimmerShape).shimmerEffect())
            }
        }
    }
}