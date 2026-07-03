package com.apexinvest.app.ui.screens

<<<<<<< HEAD
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
=======
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
<<<<<<< HEAD
import androidx.compose.foundation.layout.fillMaxHeight
=======
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
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
<<<<<<< HEAD
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
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import com.apexinvest.app.ui.navigation.Screen
import com.apexinvest.app.viewmodel.CommodityUiModel
import com.apexinvest.app.viewmodel.ExploreUiState
import com.apexinvest.app.viewmodel.ExploreViewModel
import com.apexinvest.app.viewmodel.SearchUiState
<<<<<<< HEAD
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
=======
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2

@Composable
fun ExploreScreen(
    viewModel: ExploreViewModel,
    onNavigate: (String) -> Unit,
<<<<<<< HEAD
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
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
                }
            }
        }
    }
<<<<<<< HEAD
=======

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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
}

@Composable
fun ExploreContent(
<<<<<<< HEAD
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
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
            }
        }
    }
}

@Composable
<<<<<<< HEAD
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
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
                }
            }
        }
    }
}

@Composable
<<<<<<< HEAD
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
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
        }
    }
}

@Composable
<<<<<<< HEAD
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
=======
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
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
}