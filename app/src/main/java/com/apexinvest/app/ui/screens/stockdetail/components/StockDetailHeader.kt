package com.apexinvest.app.ui.screens.stockdetail.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.apexinvest.app.api.models.StockDetailsResponse
import com.apexinvest.app.data.StockEntity
import com.apexinvest.app.ui.components.CommonScreenHeader
import com.apexinvest.app.ui.components.glassCard
import com.apexinvest.app.ui.theme.BrandPurple
import com.apexinvest.app.ui.theme.LocalAppColors
import com.apexinvest.app.util.getCurrencySymbol
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

@Composable
fun GlassTopBar(symbol: String, isFollowing: Boolean, isDark: Boolean, onBack: () -> Unit, onWatchlistToggle: () -> Unit) {
    CommonScreenHeader(
        onBackClick = onBack,
        title = symbol,
        trailingContent = {
            Box(Modifier.size(40.dp).clip(CircleShape).background(if(isDark) Color.White.copy(0.1f) else Color.Black.copy(0.05f)).clickable { onWatchlistToggle() }, contentAlignment = Alignment.Center) {
                Icon(if (isFollowing) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, contentDescription = "Watchlist", tint = if (isFollowing) BrandPurple else MaterialTheme.colorScheme.onSurface)
            }
        }
    )
}

@Composable
fun PriceHeroSection(
    title: String,
    symbol: String,
    exchangeName: String,
    price: Double,
    change: Double,
    percent: Double,
    isPositive: Boolean,
    isLoading: Boolean = false,
    currencySymbol: String = "",
    preMarketPrice: Double? = null,
    preMarketChange: Double? = null,
    postMarketPrice: Double? = null,
    postMarketChange: Double? = null,
    marketState: String? = null,
    hasPrePost: Boolean = true
) {
    val appColors = LocalAppColors.current
    val trendColor = if (isPositive) appColors.trendGreen else appColors.trendRed
    val sign = if (isPositive) "+" else ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isLoading || price == 0.0) {
            Box(Modifier.width(180.dp).height(80.dp).clip(RoundedCornerShape(16.dp)).background(MaterialTheme.colorScheme.onSurface.copy(0.05f)))
        } else {
            // Company Name & Symbol
            Text(
                text = "$title • $symbol",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(0.8f),
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            Text(
                text = exchangeName,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                fontWeight = FontWeight.Normal
            )

            Spacer(modifier = Modifier.height(16.dp))

            val isPre = marketState == "PRE" || marketState == "PREPRE"
            val isPost = marketState == "POST" || marketState == "POSTPOST" || marketState == "CLOSED"
            val isRegular = marketState == "REGULAR" || marketState == null

            // Show extended column if exchange supports it
            val showExtended = hasPrePost && (isPre || isPost || !isRegular)

            // Animated price row to avoid layout jumps
            AnimatedContent(
                targetState = Triple(price, showExtended, marketState),
                transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                label = "PriceHeaderTransition"
            ) { (currentPrice, extendedVisible, _) ->
                Row(
                    modifier = Modifier.wrapContentWidth().padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // --- LEFT COLUMN: REGULAR MARKET ---
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        com.apexinvest.app.ui.components.AppTickerText(
                            value = currentPrice,
                            currencySymbol = currencySymbol,
                            textStyle = MaterialTheme.typography.displayMedium.copy(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                letterSpacing = (-0.5).sp
                            )
                        )

                        Text(
                            text = "$sign${String.format(Locale.US, "%.2f", change)} ($sign${String.format(Locale.US, "%.2f", percent)}%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = trendColor
                        )
                    }

                    // --- SEPARATOR & EXTENDED COLUMN ---
                    if (extendedVisible) {
                        val extPrice = if (isPre) preMarketPrice else postMarketPrice
                        val extChange = if (isPre) preMarketChange else postMarketChange

                        if (extPrice != null && extPrice > 0.0) {
                            Spacer(Modifier.width(16.dp))
                            Box(Modifier.width(1.dp).height(40.dp).background(MaterialTheme.colorScheme.onSurface.copy(0.1f)))
                            Spacer(Modifier.width(16.dp))

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val extIsPositive = (extChange ?: 0.0) >= 0
                                val extTrendColor = if (extIsPositive) appColors.trendGreen else appColors.trendRed
                                val extSign = if (extIsPositive) "+" else ""
                                val extPercent = if (currentPrice > 0.0) ((extChange ?: 0.0) / currentPrice) * 100.0 else 0.0

                                Text(
                                    text = "$currencySymbol${String.format(Locale.US, "%.2f", extPrice)}",
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    letterSpacing = (-0.5).sp
                                )

                                Text(
                                    text = "$extSign${String.format(Locale.US, "%.2f", extChange)} ($extSign${String.format(Locale.US, "%.2f", extPercent)}%)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = extTrendColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickMetricsStrip(data: StockDetailsResponse, isDark: Boolean) {
    val items = listOf(
        "Open" to (data.marketPricing?.priceOpen?.let { String.format(Locale.US, "%.2f", it) } ?: "-"),
        "High" to (data.marketPricing?.priceHigh?.let { String.format(Locale.US, "%.2f", it) } ?: "-"),
        "Low" to (data.marketPricing?.priceLow?.let { String.format(Locale.US, "%.2f", it) } ?: "-"),
        "52W High" to (data.marketPricing?.high52Week?.let { String.format(Locale.US, "%.2f", it) } ?: "-"),
        "52W Low" to (data.marketPricing?.low52Week?.let { String.format(Locale.US, "%.2f", it) } ?: "-")
    )

    LazyRow(Modifier.fillMaxWidth(), contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        items(items.size) { i -> StripCard(items[i].first, items[i].second, isDark) }
    }
}

@Composable
fun StripCard(label: String, value: String, isDark: Boolean) {
    Column(Modifier.width(100.dp).glassCard(isDark, RoundedCornerShape(16.dp)).padding(12.dp)) {
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
fun AiApexInvestCard(report: String?, isDark: Boolean) {
    if (report == null) return
    Box(Modifier.fillMaxWidth().padding(horizontal = 20.dp).glassCard(isDark, RoundedCornerShape(24.dp)).border(1.dp, Brush.linearGradient(listOf(BrandPurple.copy(0.5f), Color.Transparent)), RoundedCornerShape(24.dp)).padding(20.dp)) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(28.dp).clip(CircleShape).background(BrandPurple.copy(0.2f)), contentAlignment = Alignment.Center) { Icon(Icons.Default.AutoAwesome, null, tint = BrandPurple, modifier = Modifier.size(16.dp)) }
                Spacer(Modifier.width(12.dp))
                Text("Apex Invest Insights", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if(isDark) Color.White else BrandPurple)
            }
            Spacer(Modifier.height(12.dp))
            Text(report, fontSize = 14.sp, lineHeight = 22.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun StockHoldingsCard(
    holding: StockEntity,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val symbol = holding.symbol
    val exchangeInfo = com.apexinvest.app.util.StockMetadataUtils.getExchangeInfo(symbol)
    val currencySym = getCurrencySymbol(exchangeInfo.currency)

    val totalValue = holding.currentPrice * holding.quantity
    val investedValue = holding.buyPrice * holding.quantity
    val returns = totalValue - investedValue
    val returnsPct = if (investedValue != 0.0) (returns / investedValue) * 100.0 else 0.0
    val isPositive = returns >= 0

    val appColors = LocalAppColors.current
    val trendColor = if (isPositive) appColors.trendGreen else appColors.trendRed

    Column(
        modifier = modifier
            .fillMaxWidth()
            .glassCard(isDark, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = BrandPurple,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Your Position",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = "${if (isPositive) "+" else ""}${currencySym}${String.format(Locale.US, "%.2f", returns)} (${String.format(Locale.US, "%.2f", returnsPct)}%)",
                color = trendColor,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Total Value",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${currencySym}${String.format(Locale.US, "%.2f", totalValue)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Invested",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${currencySym}${String.format(Locale.US, "%.2f", investedValue)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(Modifier.height(16.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(0.05f))
        )
        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    "Quantity",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    String.format(Locale.US, "%.2f", holding.quantity),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "Avg. Price",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${currencySym}${String.format(Locale.US, "%.2f", holding.buyPrice)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun SwipeToTradeButton(
    text: String,
    color: Color,
    isDark: Boolean,
    enabled: Boolean,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val handleOffset = remember { Animatable(0f) }
    var trackWidth by remember { mutableFloatStateOf(0f) }
    val handleSize = 48.dp
    val handleSizePx = with(density) { handleSize.toPx() }

    val alpha = if (enabled) 1f else 0.3f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .alpha(alpha)
            .clip(RoundedCornerShape(16.dp))
            .background(if (isDark) Color.White.copy(0.05f) else Color.Black.copy(0.03f))
            .onSizeChanged { trackWidth = it.width.toFloat() },
        contentAlignment = Alignment.CenterStart
    ) {
        // Track Text
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth(),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Black,
            color = if (isDark) Color.White.copy(0.3f) else Color.Black.copy(0.2f),
            letterSpacing = 1.sp
        )

        // The Handle
        Box(
            modifier = Modifier
                .offset { IntOffset(handleOffset.value.roundToInt(), 0) }
                .size(handleSize)
                .padding(4.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(color)
                .pointerInput(enabled) {
                    if (!enabled) return@pointerInput
                    detectDragGestures(
                        onDragEnd = {
                            val progress = handleOffset.value / (trackWidth - handleSizePx)
                            if (progress > 0.8f) {
                                scope.launch {
                                    handleOffset.animateTo(trackWidth - handleSizePx)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onComplete()
                                    delay(500.milliseconds)
                                    handleOffset.snapTo(0f)
                                }
                            } else {
                                scope.launch { handleOffset.animateTo(0f) }
                            }
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val next = (handleOffset.value + dragAmount.x).coerceIn(0f, trackWidth - handleSizePx)
                            scope.launch { handleOffset.snapTo(next) }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun TradeActionBar(
    isDark: Boolean,
    canSell: Boolean,
    marketStatus: Pair<Boolean, String>,
    onSellComplete: () -> Unit,
    onBuyComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val appColors = LocalAppColors.current
    val currentOnSellComplete by rememberUpdatedState(onSellComplete)
    val currentOnBuyComplete by rememberUpdatedState(onBuyComplete)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.98f))
            .padding(top = 8.dp, bottom = 24.dp)
    ) {
        // Market Status Indicator
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(if (marketStatus.first) appColors.trendGreen else appColors.trendRed))
            Spacer(Modifier.width(8.dp))
            Text(
                marketStatus.second,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }

        Row(
            modifier = Modifier.padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val isMarketOpen = marketStatus.first

            SwipeToTradeButton(
                text = if (isMarketOpen) "SWIPE TO SELL" else "MARKET CLOSED",
                color = appColors.trendRed,
                isDark = isDark,
                enabled = canSell && isMarketOpen,
                onComplete = currentOnSellComplete,
                modifier = Modifier.weight(1f)
            )

            SwipeToTradeButton(
                text = if (isMarketOpen) "SWIPE TO BUY" else "MARKET CLOSED",
                color = appColors.trendGreen,
                isDark = isDark,
                enabled = isMarketOpen,
                onComplete = currentOnBuyComplete,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
