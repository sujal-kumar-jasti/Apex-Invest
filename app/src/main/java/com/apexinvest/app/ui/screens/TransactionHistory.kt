package com.apexinvest.app.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apexinvest.app.R
import com.apexinvest.app.ui.theme.BrandPurple
import com.apexinvest.app.ui.theme.LocalAppColors
import com.apexinvest.app.util.getCurrencySymbol
import com.apexinvest.app.util.toCleanString
import com.apexinvest.app.viewmodel.PortfolioViewModel
import com.apexinvest.app.viewmodel.TransactionUiModel
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.LinearProgressIndicator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.time.Duration.Companion.milliseconds
import android.graphics.Color as AndroidColor

const val TAG = "RecyclerPerf"

@SuppressLint("InflateParams", "SetTextI18n")
@Composable
fun TransactionHistory(
    portfolioViewModel: PortfolioViewModel,
    onBack: () -> Unit,
    isConnected: Boolean
) {
    val scope = rememberCoroutineScope()
    val appColors = LocalAppColors.current
    val context = LocalContext.current

    var isSyncing by rememberSaveable { mutableStateOf(false) }
    var showConfirmDialog by rememberSaveable { mutableStateOf(false) }
    var isScreenLoading by rememberSaveable { mutableStateOf(true) }

    val selectedIds = rememberSaveable(
        saver = listSaver(save = { it.toList() }, restore = { it.toMutableStateList() })
    ) { mutableStateListOf<Int>() }

    val isSelectionMode by remember { derivedStateOf { selectedIds.isNotEmpty() } }

    BackHandler {
        if (isSelectionMode) {
            selectedIds.clear()
        } else {
            onBack()
        }
    }

    LaunchedEffect(Unit) {
        Log.d(TAG, "🚀 XML SCREEN_OPENED")
        delay(350.milliseconds)
        isScreenLoading = false
    }

    val currentSelection = remember(selectedIds.size, selectedIds.toList()) { selectedIds.toSet() }

    val uiState by portfolioViewModel.uiState.collectAsStateWithLifecycle()
    val analyticsState by portfolioViewModel.transactionAnalyticsState.collectAsStateWithLifecycle()
    val currencySymbol = remember(uiState.isUsd) { getCurrencySymbol(if (uiState.isUsd) "USD" else "INR") }

    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    // --- MESH BACKGROUND BRUSH ---
    val meshBrush = remember(isDark) {
        Brush.verticalGradient(
            listOf(BrandPurple.copy(alpha = if (isDark) 0.12f else 0.05f), Color.Transparent)
        )
    }

    val colorGreen = appColors.trendGreen.toArgb()
    val colorRed = appColors.trendRed.toArgb()
    val colorPurple = BrandPurple.toArgb()
    val onSurfaceColorInt = MaterialTheme.colorScheme.onSurface.toArgb()

    // --- EXACT GLASSY COLORS FROM DASHBOARD ---
    val themeSurface = MaterialTheme.colorScheme.surfaceVariant
    val themeOutline = MaterialTheme.colorScheme.outlineVariant

    val surfaceVariantInt = themeSurface.copy(alpha = if (isDark) 0.4f else 0.6f).toArgb()
    val outlineVariantInt = themeOutline.copy(alpha = if (isDark) 0.6f else 0.8f).toArgb()
    val pillSurfaceInt = themeSurface.copy(alpha = if (isDark) 0.4f else 0.6f).toArgb()

    val rippleColorInt = if (isDark) AndroidColor.argb(50, 255, 255, 255) else AndroidColor.argb(50, 0, 0, 0)

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Delete Transactions?", fontWeight = FontWeight.Bold) },
            text = { Text("Permanently remove ${selectedIds.size} records from your history?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch {
                            portfolioViewModel.deleteSelectedTransactions(currentSelection)
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

    val adapter = remember(isDark) {
        TransactionXmlAdapter(
            context = context,
            colorGreen = colorGreen,
            colorRed = colorRed,
            colorPurple = colorPurple,
            onSurfaceColor = onSurfaceColorInt,
            surfaceVariant = surfaceVariantInt,
            outlineVariant = outlineVariantInt,
            pillSurface = pillSurfaceInt,
            onItemClick = { item ->
                if (isSelectionMode) {
                    if (selectedIds.contains(item.id)) selectedIds.remove(item.id)
                    else selectedIds.add(item.id)
                }
            },
            onItemLongClick = { item ->
                if (!isSelectionMode) selectedIds.add(item.id)
            }
        )
    }

    LaunchedEffect(
        analyticsState.mappedHistory,
        isSelectionMode,
        currentSelection,
        isScreenLoading,
        adapter
    ) {
        if (analyticsState.isInitial || analyticsState.mappedHistory.isEmpty() || isScreenLoading) return@LaunchedEffect

        val fullList = analyticsState.mappedHistory
        val isFirstLoad = adapter.items.isEmpty()

        if (isFirstLoad && fullList.size > 20) {
            adapter.updateData(
                fullList.take(20),
                currentSelection,
                isSelectionMode,
                currencySymbol,
                analyticsState.totalBuy,
                analyticsState.totalSell
            )
            delay(300.milliseconds)
        }

        adapter.updateData(
            fullList,
            currentSelection,
            isSelectionMode,
            currencySymbol,
            analyticsState.totalBuy,
            analyticsState.totalSell
        )
    }

    AndroidView(
        modifier = Modifier
            .fillMaxSize()
            .background(meshBrush) // 🚀 Mesh background applied here!
            .then(if (isConnected) Modifier.statusBarsPadding() else Modifier),
        factory = { ctx ->
            val themedCtx = android.view.ContextThemeWrapper(
                ctx,
                com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar
            )
            val view = LayoutInflater.from(themedCtx).inflate(R.layout.activity_transaction_history, null)

            val btnBack = view.findViewById<MaterialButton>(R.id.btnBack)
            val btnSync = view.findViewById<MaterialButton>(R.id.btnSync)
            val btnClose = view.findViewById<MaterialButton>(R.id.btnCloseSelection)
            val btnDelete = view.findViewById<MaterialButton>(R.id.btnDelete)
            val rv = view.findViewById<RecyclerView>(R.id.rvTransactions)

            val density = view.context.resources.displayMetrics.density
            val btnSizePx = (40 * density).toInt()
            val btnRadius = (20 * density).toInt()
            val iconSizePx = (20 * density).toInt()

            val styleButton: (MaterialButton) -> Unit = { btn ->
                btn.layoutParams.width = btnSizePx
                btn.layoutParams.height = btnSizePx
                btn.strokeWidth = 2
                btn.cornerRadius = btnRadius
                btn.insetTop = 0
                btn.insetBottom = 0
                btn.setPadding(0, 0, 0, 0)
                btn.iconPadding = 0
                btn.iconSize = iconSizePx
                btn.iconGravity = MaterialButton.ICON_GRAVITY_TEXT_START
                btn.rippleColor = ColorStateList.valueOf(rippleColorInt)
            }

            styleButton(btnBack)
            styleButton(btnSync)
            styleButton(btnClose)
            styleButton(btnDelete)

            rv.layoutManager = LinearLayoutManager(themedCtx).apply {
                isItemPrefetchEnabled = true
                initialPrefetchItemCount = 4
            }
            rv.setItemViewCacheSize(5)
            rv.setHasFixedSize(true)
            rv.itemAnimator = null

            btnBack.setOnClickListener { onBack() }
            btnClose.setOnClickListener { selectedIds.clear() }
            btnDelete.setOnClickListener { showConfirmDialog = true }

            view.findViewById<MaterialButton>(R.id.btnSelectAll).setOnClickListener {
                if (selectedIds.size == analyticsState.mappedHistory.size) {
                    selectedIds.clear()
                } else {
                    selectedIds.clear()
                    selectedIds.addAll(analyticsState.mappedHistory.map { it.id })
                }
            }

            btnSync.setOnClickListener {
                scope.launch {
                    isSyncing = true
                    portfolioViewModel.loadPortfolioAndPrices()
                    delay(400.milliseconds)
                    isSyncing = false
                }
            }
            view
        },
        update = { view ->
            val groupDefault = view.findViewById<Group>(R.id.groupDefaultMode)
            val groupSelection = view.findViewById<Group>(R.id.groupSelectionMode)
            val tvSelectionCount = view.findViewById<TextView>(R.id.tvSelectionCount)
            val tvHeaderTitle = view.findViewById<TextView>(R.id.tvHeaderTitle)
            val btnSelectAll = view.findViewById<MaterialButton>(R.id.btnSelectAll)
            val btnSync = view.findViewById<MaterialButton>(R.id.btnSync)
            val btnBack = view.findViewById<MaterialButton>(R.id.btnBack)
            val btnClose = view.findViewById<MaterialButton>(R.id.btnCloseSelection)
            val btnDelete = view.findViewById<MaterialButton>(R.id.btnDelete)
            val progressSync = view.findViewById<ProgressBar>(R.id.progressSync)
            val rv = view.findViewById<RecyclerView>(R.id.rvTransactions)
            val tvEmptyState = view.findViewById<TextView>(R.id.tvEmptyState)
            val progressMain = view.findViewById<ProgressBar>(R.id.progressMain)

            if (rv.adapter != adapter) {
                rv.adapter = adapter
            }

            tvHeaderTitle.setTextColor(onSurfaceColorInt)
            tvSelectionCount.setTextColor(onSurfaceColorInt)
            tvEmptyState.setTextColor(AndroidColor.GRAY)

            btnClose.backgroundTintList = ColorStateList.valueOf(surfaceVariantInt)
            btnClose.strokeColor = ColorStateList.valueOf(outlineVariantInt)
            btnClose.iconTint = ColorStateList.valueOf(colorRed)

            btnDelete.backgroundTintList = ColorStateList.valueOf(surfaceVariantInt)
            btnDelete.strokeColor = ColorStateList.valueOf(outlineVariantInt)
            btnDelete.iconTint = ColorStateList.valueOf(colorRed)

            btnBack.backgroundTintList = ColorStateList.valueOf(surfaceVariantInt)
            btnBack.strokeColor = ColorStateList.valueOf(outlineVariantInt)
            btnBack.iconTint = ColorStateList.valueOf(onSurfaceColorInt)

            btnSync.backgroundTintList = ColorStateList.valueOf(surfaceVariantInt)
            btnSync.strokeColor = ColorStateList.valueOf(outlineVariantInt)
            btnSync.iconTint = ColorStateList.valueOf(colorPurple)

            btnBack.isVisible = !isSelectionMode
            btnSync.isVisible = !isSelectionMode && !isSyncing
            progressSync.isVisible = !isSelectionMode && isSyncing

            btnClose.isVisible = isSelectionMode
            btnDelete.isVisible = isSelectionMode

            tvHeaderTitle.isVisible = !isSelectionMode

            groupDefault.isVisible = !isSelectionMode
            groupSelection.isVisible = isSelectionMode

            if (isSelectionMode) {
                tvSelectionCount.text = "${selectedIds.size} Selected"
                btnSelectAll.text = if (selectedIds.size == analyticsState.mappedHistory.size) "None" else "All"
                btnSelectAll.setTextColor(onSurfaceColorInt)
            }

            val isDataLoading = analyticsState.isInitial || isScreenLoading
            progressMain.isVisible = isDataLoading

            val showEmpty = !isDataLoading && analyticsState.mappedHistory.isEmpty()
            tvEmptyState.isVisible = showEmpty
            rv.isVisible = !isDataLoading && !showEmpty
        }
    )
}

class TransactionXmlAdapter(
    context: Context,
    private val colorGreen: Int,
    private val colorRed: Int,
    private val colorPurple: Int,
    private val onSurfaceColor: Int,
    private val surfaceVariant: Int,
    private val outlineVariant: Int,
    private val pillSurface: Int,
    private val onItemClick: (TransactionUiModel) -> Unit,
    private val onItemLongClick: (TransactionUiModel) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        const val TYPE_SUMMARY = 0
        const val TYPE_ROW = 1
    }

    private val themedCtx = android.view.ContextThemeWrapper(context, com.google.android.material.R.style.Theme_Material3_DayNight_NoActionBar)
    private val inflater = LayoutInflater.from(themedCtx)

    private val greenAlphaList = ColorStateList.valueOf(AndroidColor.argb(38, AndroidColor.red(colorGreen), AndroidColor.green(colorGreen), AndroidColor.blue(colorGreen)))
    private val redAlphaList = ColorStateList.valueOf(AndroidColor.argb(38, AndroidColor.red(colorRed), AndroidColor.green(colorRed), AndroidColor.blue(colorRed)))
    private val selectionPurpleAlpha = AndroidColor.argb(30, AndroidColor.red(colorPurple), AndroidColor.green(colorPurple), AndroidColor.blue(colorPurple))
    private val selectionPurpleList = ColorStateList.valueOf(colorPurple)

    var items = listOf<TransactionUiModel>()
        private set
    var selectedIds = setOf<Int>()
        private set
    var isSelectionMode = false
        private set

    private var currencySymbol = "$"
    private var totalBuy = 0.0
    private var totalSell = 0.0

    @SuppressLint("NotifyDataSetChanged")
    suspend fun updateData(
        newItems: List<TransactionUiModel>,
        newSelectedIds: Set<Int>,
        newIsSelectionMode: Boolean,
        newCurrency: String,
        newTotalBuy: Double,
        newTotalSell: Double
    ) {
        val oldItems = this.items
        val oldMode = this.isSelectionMode

        if (oldItems.isEmpty() || newItems.size <= 20 || oldMode != newIsSelectionMode) {
            withContext(Dispatchers.Main) {
                this@TransactionXmlAdapter.items = newItems
                this@TransactionXmlAdapter.selectedIds = newSelectedIds
                this@TransactionXmlAdapter.isSelectionMode = newIsSelectionMode
                this@TransactionXmlAdapter.currencySymbol = newCurrency
                this@TransactionXmlAdapter.totalBuy = newTotalBuy
                this@TransactionXmlAdapter.totalSell = newTotalSell
                notifyDataSetChanged()
            }
            return
        }

    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0 && !isSelectionMode) TYPE_SUMMARY else TYPE_ROW
    }

    override fun getItemCount(): Int {
        return if (isSelectionMode) items.size else if (items.isNotEmpty()) items.size + 1 else 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_SUMMARY) {
            SummaryViewHolder(inflater.inflate(R.layout.item_transaction_summary, parent, false))
        } else {
            RowViewHolder(inflater.inflate(R.layout.item_transaction_row, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is SummaryViewHolder) {
            holder.bind()
        } else if (holder is RowViewHolder) {
            val itemPosition = if (isSelectionMode) position else position - 1
            holder.bind(items[itemPosition])
        }
    }

    inner class SummaryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val rootCard = view as MaterialCardView
        private val layoutPills = view.findViewById<LinearLayout>(R.id.layoutPills)
        private val tvNetFlowTitle = view.findViewById<TextView>(R.id.tvNetFlowTitle)
        private val tvNetFlowSubtitle = view.findViewById<TextView>(R.id.tvNetFlowSubtitle)
        private val tvNetFlowValue = view.findViewById<TextView>(R.id.tvNetFlowValue)
        private val tvTotalInvested = view.findViewById<TextView>(R.id.tvTotalInvested)
        private val tvTotalLiquidated = view.findViewById<TextView>(R.id.tvTotalLiquidated)
        private val progressDistribution = view.findViewById<LinearProgressIndicator>(R.id.progressDistribution)
        private val tvBuyRatio = view.findViewById<TextView>(R.id.tvBuyRatio)
        private val tvSellRatio = view.findViewById<TextView>(R.id.tvSellRatio)
        private val ivWalletIcon = (view.findViewById<View>(R.id.iconWalletBg) as? ViewGroup)?.getChildAt(0) as? ImageView

        init {
            (rootCard.getChildAt(0) as? ViewGroup)?.setBackgroundColor(AndroidColor.TRANSPARENT)
        }

        @SuppressLint("SetTextI18n")
        fun bind() {
            rootCard.cardElevation = 0f
            rootCard.setCardBackgroundColor(surfaceVariant)
            rootCard.strokeColor = outlineVariant
            rootCard.strokeWidth = 2

            tvNetFlowTitle.setTextColor(onSurfaceColor)
            tvTotalInvested.setTextColor(onSurfaceColor)
            tvTotalLiquidated.setTextColor(onSurfaceColor)

            ivWalletIcon?.setColorFilter(colorPurple)

            if (layoutPills != null && layoutPills.childCount >= 2) {
                val investedPill = layoutPills.getChildAt(0) as? MaterialCardView
                val liquidatedPill = layoutPills.getChildAt(1) as? MaterialCardView

                investedPill?.cardElevation = 0f
                investedPill?.setCardBackgroundColor(pillSurface)
                investedPill?.strokeColor = outlineVariant

                liquidatedPill?.cardElevation = 0f
                liquidatedPill?.setCardBackgroundColor(pillSurface)
                liquidatedPill?.strokeColor = outlineVariant
            }

            val netFlow = totalBuy - totalSell
            val isNetPositive = netFlow >= 0
            val totalVolume = totalBuy + totalSell
            val buyRatio = if (totalVolume > 0) (totalBuy / totalVolume).toFloat() else 0.5f

            tvNetFlowSubtitle.text = if (isNetPositive) "Capital Flow In" else "Capital Flow Out"
            tvNetFlowValue.text = "${if (isNetPositive) "+" else "-"}$currencySymbol${abs(netFlow).toCleanString()}"
            tvNetFlowValue.setTextColor(if (isNetPositive) colorGreen else onSurfaceColor)

            tvTotalInvested.text = "$currencySymbol${totalBuy.toCleanString()}"
            tvTotalLiquidated.text = "$currencySymbol${totalSell.toCleanString()}"

            val buyPercentage = (buyRatio * 100).toInt()
            progressDistribution.progress = buyPercentage
            progressDistribution.setIndicatorColor(colorGreen)
            progressDistribution.trackColor = colorRed

            tvBuyRatio.text = "Buy $buyPercentage%"
            tvBuyRatio.setTextColor(colorGreen)
            tvSellRatio.text = "Sell ${100 - buyPercentage}%"
            tvSellRatio.setTextColor(colorRed)
        }
    }

    inner class RowViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val rootCard = view as MaterialCardView
        private val cbSelect = view.findViewById<CheckBox>(R.id.cbSelect)
        private val iconTrendBg = view.findViewById<View>(R.id.iconTrendBg)
        private val ivTrendIcon = view.findViewById<ImageView>(R.id.ivTrendIcon)
        private val tvSymbol = view.findViewById<TextView>(R.id.tvSymbol)
        private val tvBuySellPill = view.findViewById<TextView>(R.id.tvBuySellPill)
        private val tvUnitsDate = view.findViewById<TextView>(R.id.tvUnitsDate)
        private val tvTotalValue = view.findViewById<TextView>(R.id.tvTotalValue)
        private val tvPrice = view.findViewById<TextView>(R.id.tvPrice)

        init {
            rootCard.cardElevation = 0f
            (rootCard.getChildAt(0) as? ViewGroup)?.setBackgroundColor(AndroidColor.TRANSPARENT)
        }

        @SuppressLint("SetTextI18n")
        fun bind(item: TransactionUiModel) {
            val trendColor = if (item.isBuy) colorGreen else colorRed
            val isSelected = selectedIds.contains(item.id)

            tvSymbol.text = item.symbol
            tvSymbol.setTextColor(onSurfaceColor)

            tvUnitsDate.text = "${item.quantityStr} Units  •  ${item.formattedDate}"
            tvTotalValue.text = "${if (item.isBuy) "+" else "-"}$currencySymbol${item.totalValue.toCleanString()}"
            tvPrice.text = "@ $currencySymbol${item.convertedPrice.toCleanString()}"

            tvBuySellPill.text = if (item.isBuy) "BUY" else "SELL"
            tvBuySellPill.setTextColor(trendColor)
            tvTotalValue.setTextColor(trendColor)

            ivTrendIcon.setColorFilter(trendColor)
            ivTrendIcon.setImageResource(if (item.isBuy) R.drawable.ic_trending_up else R.drawable.ic_trending_down)

            val cachedAlphaList = if (item.isBuy) greenAlphaList else redAlphaList
            iconTrendBg.backgroundTintList = cachedAlphaList
            tvBuySellPill.backgroundTintList = cachedAlphaList

            cbSelect.isVisible = isSelectionMode
            cbSelect.isChecked = isSelected
            cbSelect.buttonTintList = selectionPurpleList

            if (isSelected) {
                rootCard.setCardBackgroundColor(selectionPurpleAlpha)
                rootCard.strokeColor = colorPurple
                rootCard.strokeWidth = 3
            } else {
                rootCard.setCardBackgroundColor(surfaceVariant)
                rootCard.strokeColor = outlineVariant
                rootCard.strokeWidth = 2
            }

            rootCard.setOnClickListener { onItemClick(item) }
            rootCard.setOnLongClickListener {
                onItemLongClick(item)
                true
            }
        }
    }
}