package com.apexinvest.app.ui.screens

import android.content.ContentValues
<<<<<<< HEAD
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
=======
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
<<<<<<< HEAD
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.apexinvest.app.ui.components.CommonScreenHeader
import com.apexinvest.app.ui.components.glassCard
import com.apexinvest.app.ui.theme.BrandPurple
import com.apexinvest.app.ui.theme.LocalAppColors
import com.apexinvest.app.util.getCurrencySymbol
import com.apexinvest.app.viewmodel.AuthState
import com.apexinvest.app.viewmodel.AuthViewModel
import com.apexinvest.app.viewmodel.PortfolioViewModel
import kotlinx.coroutines.flow.collectLatest
=======
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.apexinvest.app.viewmodel.AuthViewModel
import com.apexinvest.app.viewmodel.PortfolioViewModel
import com.google.firebase.auth.FirebaseAuth
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    portfolioViewModel: PortfolioViewModel,
<<<<<<< HEAD
    predictionViewModel: com.apexinvest.app.viewmodel.PredictionViewModel,
    exploreViewModel: com.apexinvest.app.viewmodel.ExploreViewModel,
    onBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    isConnected: Boolean,
) {
    val authState by authViewModel.authState.collectAsState()
    val state by portfolioViewModel.uiState.collectAsState()
    val notificationsEnabled by portfolioViewModel.notificationsEnabled.collectAsState()
    val themeMode by portfolioViewModel.themeMode.collectAsState()

    // 🆕 Trading Presets
    val defaultBuyQty by portfolioViewModel.defaultBuyQty.collectAsState()
    val defaultSellQty by portfolioViewModel.defaultSellQty.collectAsState()
    var showPresetDialog by remember { mutableStateOf(false) }
    var tempBuyQty by remember { mutableStateOf(defaultBuyQty.toString()) }
    var tempSellQty by remember { mutableStateOf(defaultSellQty.toString()) }

    val isGoogleUser = remember { authViewModel.isGoogleUser() }
    val isExporting = state.isLoading
=======
    onBack: () -> Unit,
    isConnected: Boolean
) {
    val user = FirebaseAuth.getInstance().currentUser
    val state by portfolioViewModel.uiState.collectAsState()

    // 1. UPDATED: Observe Notifications from ViewModel (Syncs with Dashboard)
    val notificationsEnabled by portfolioViewModel.notificationsEnabled.collectAsState()

    val isPriceUpdating = state.isLoading

>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

<<<<<<< HEAD
    val userEmail = remember { authViewModel.getUserEmail() ?: "trader@apexinvest.com" }

    var showPasswordModal by remember { mutableStateOf(value = false) }
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val isDark = remember(surfaceColor) { surfaceColor.luminance() < 0.5f }

    val meshBrush = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(listOf(BrandPurple.copy(alpha = 0.12f), Color.Transparent))
        } else {
            Brush.verticalGradient(listOf(BrandPurple.copy(alpha = 0.05f), Color.Transparent))
        }
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as android.app.Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !isDark
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.LoggedOut) {
            onNavigateToLogin()
        }
        if (authState is AuthState.SuccessMessage) {
            Toast.makeText(context, (authState as AuthState.SuccessMessage).message, Toast.LENGTH_SHORT).show()
            showPasswordModal = false
            oldPassword = ""
            newPassword = ""
            authViewModel.clearError()
        }
    }

    val onExportClick: () -> Unit = {
        scope.launch {
            portfolioViewModel.refreshPricesAndGenerateCsv().collectLatest { csvContent ->
                try {
=======
    // --- LOGIC: CSV EXPORT ---
    val onExportClick: () -> Unit = {
        scope.launch {
            val result = portfolioViewModel.refreshPricesAndGenerateCsv()
            result.fold(
                onSuccess = { csvContent ->
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
                    val fileName = "ApexInvest_Portfolio_${LocalDate.now()}.csv"
                    val contentValues = ContentValues().apply {
                        put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                        put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
<<<<<<< HEAD
                        put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    }
                    val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

                    if (uri != null) {
                        context.contentResolver.openOutputStream(uri)?.use { it.write(csvContent.toByteArray()) }
                        Toast.makeText(context, "Saved to Downloads", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error saving file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    if (showPasswordModal) {
        ChangePasswordDialog(
            oldPassword = oldPassword,
            newPassword = newPassword,
            onOldPasswordChange = { oldPassword = it },
            onNewPasswordChange = { newPassword = it },
            onDismiss = { showPasswordModal = false },
            onConfirm = { authViewModel.updatePassword(oldPassword, newPassword) },
            isLoading = authState is AuthState.Loading,
            errorMessage = if (authState is AuthState.Error) (authState as AuthState.Error).message else null
        )
    }

    if (showDeleteAccountDialog) {
        val appColors = LocalAppColors.current
        AlertDialog(
            onDismissRequest = { showDeleteAccountDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            icon = { Icon(Icons.Default.Warning, null, tint = appColors.trendRed) },
            title = { Text("Delete Account?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This action is permanent. All your portfolio data, transaction history, and watchlist items will be wiped from our servers immediately.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteAccountDialog = false
                        authViewModel.performDeleteAccount(
                            portfolioViewModel = portfolioViewModel,
                            predictionViewModel = predictionViewModel,
                            exploreViewModel = exploreViewModel
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.trendRed)
                ) {
                    Text("Delete Forever", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAccountDialog = false }) {
                    Text("Cancel", fontWeight = FontWeight.Bold, color = BrandPurple)
                }
            }
        )
    }

    if (showPresetDialog) {
        AlertDialog(
            onDismissRequest = { showPresetDialog = false },
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Trading Presets", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Set default share quantities for instant trading.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = tempBuyQty,
                        onValueChange = { tempBuyQty = it },
                        label = { Text("Default Buy Quantity") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempSellQty,
                        onValueChange = { tempSellQty = it },
                        label = { Text("Default Sell Quantity") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val b = tempBuyQty.toDoubleOrNull() ?: defaultBuyQty
                        val s = tempSellQty.toDoubleOrNull() ?: defaultSellQty
                        portfolioViewModel.setTradingPresets(b, s)
                        showPresetDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
                ) {
                    Text("Save Changes", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPresetDialog = false }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(meshBrush)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            CommonScreenHeader(
                title = "Profile & Settings",
                onBackClick = onBack,
                applyStatusBarsPadding = isConnected
            )
=======
                        put(
                            MediaStore.MediaColumns.RELATIVE_PATH,
                            android.os.Environment.DIRECTORY_DOWNLOADS
                        )
                    }
                    val uri = context.contentResolver.insert(
                        MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                        contentValues
                    )
                    if (uri != null) {
                        context.contentResolver.openOutputStream(uri)
                            ?.use { it.write(csvContent.toByteArray()) }
                        Toast.makeText(context, "Saved to Downloads", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Permission Error", Toast.LENGTH_SHORT).show()
                    }
                },
                onFailure = {
                    Toast.makeText(
                        context,
                        "Export Failed: ${it.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            )
        }
    }

    Scaffold(
        // Dynamic Insets: 0dp when Offline (Banner pushes us down), Default when Online
        contentWindowInsets = if (!isConnected) {
            WindowInsets(0.dp)
        } else {
            ScaffoldDefaults.contentWindowInsets
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Profile & Settings",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
<<<<<<< HEAD
                    .padding(horizontal = 20.dp)
            ) {
                UserProfileCard(userEmail, isDark)

                Spacer(Modifier.height(32.dp))

                SectionTitle("General")
                SettingsGroupCard(isDark) {
                    SettingsTile(
                        icon = Icons.Default.AttachMoney,
                        title = "Currency",
                        subtitle = if (state.isUsd) {
                            "Displaying USD (${getCurrencySymbol("USD")})"
                        } else {
                            "Displaying INR (${getCurrencySymbol("INR")})"
                        },
                        onClick = { portfolioViewModel.toggleCurrency() },
                        trailing = {
                            Switch(
                                checked = state.isUsd,
                                onCheckedChange = { portfolioViewModel.toggleCurrency() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.background,
                                    checkedTrackColor = BrandPurple
                                )
                            )
                        }
                    )

                    DividerLine(isDark)

                    SettingsTile(
                        icon = Icons.Default.Notifications,
                        title = "Price Alerts",
                        subtitle = if (notificationsEnabled) "Notifications Active" else "Notifications Paused",
                        onClick = { portfolioViewModel.toggleNotifications() },
                        trailing = {
                            Switch(
                                checked = notificationsEnabled,
                                onCheckedChange = { portfolioViewModel.toggleNotifications() },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = MaterialTheme.colorScheme.background,
                                    checkedTrackColor = BrandPurple
                                )
                            )
                        }
                    )
                }

                Spacer(Modifier.height(24.dp))

                SectionTitle("Trading Engine")
                SettingsGroupCard(isDark) {
                    SettingsTile(
                        icon = Icons.Default.SwapHoriz,
                        title = "Trading Presets",
                        subtitle = "Buy: ${defaultBuyQty.toInt()} • Sell: ${defaultSellQty.toInt()} shares",
                        onClick = { 
                            tempBuyQty = defaultBuyQty.toString()
                            tempSellQty = defaultSellQty.toString()
                            showPresetDialog = true
                        }
                    )
                }

                Spacer(Modifier.height(24.dp))

                SectionTitle("Appearance")
                SettingsGroupCard(isDark) {
                    SettingsTile(
                        icon = when(themeMode) {
                            1 -> Icons.Default.LightMode
                            2 -> Icons.Default.DarkMode
                            else -> Icons.Default.SettingsSuggest
                        },
                        title = "Theme Mode",
                        subtitle = when(themeMode) {
                            1 -> "Light Mode"
                            2 -> "Dark Mode"
                            else -> "System Default"
                        },
                        onClick = {
                            val nextMode = (themeMode + 1) % 3
                            portfolioViewModel.setThemeMode(nextMode)
                        }
                    )
                }

                Spacer(Modifier.height(24.dp))

                SectionTitle("Security & Account")
                SettingsGroupCard(isDark) {
                    if (!isGoogleUser) {
                        SettingsTile(
                            icon = Icons.Default.Lock,
                            title = "Change Password",
                            subtitle = "Update your account security",
                            onClick = { showPasswordModal = true }
                        )
                        DividerLine(isDark)
                    }

                    SettingsTile(
                        icon = Icons.Default.FileDownload,
                        title = "Export Portfolio",
                        subtitle = if (isExporting) "Generating CSV..." else "Save data to Downloads",
                        onClick = onExportClick,
                        trailing = {
                            if (isExporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = BrandPurple
                                )
                            } else {
                                Icon(Icons.Default.Download, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    )

                    DividerLine(isDark)

                    val appColors = LocalAppColors.current
                    SettingsTile(
                        icon = Icons.Default.DeleteForever,
                        title = "Delete Account",
                        subtitle = "Permanently remove your data",
                        textColor = appColors.trendRed,
                        iconTint = appColors.trendRed,
                        onClick = { showDeleteAccountDialog = true }
                    )

                    DividerLine(isDark)

                    SettingsTile(
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        title = "Sign Out",
                        subtitle = "Log out of your account",
                        textColor = MaterialTheme.colorScheme.onSurface,
                        onClick = {
                            authViewModel.performLogout(
                                portfolioViewModel = portfolioViewModel,
                                predictionViewModel = predictionViewModel,
                                exploreViewModel = exploreViewModel
                            )
                        }
                    )
                }

                Spacer(Modifier.height(48.dp))

                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        "Apex Invest Build v1.4.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordDialog(
    oldPassword: String,
    newPassword: String,
    onOldPasswordChange: (String) -> Unit,
    onNewPasswordChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    val appColors = LocalAppColors.current
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !isLoading,
                colors = ButtonDefaults.buttonColors(containerColor = BrandPurple)
            ) {
                if (isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                else Text("Update", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Cancel", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        },
        title = { Text("Change Password", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black) },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Text("Ensure your new password is secure.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Spacer(Modifier.height(24.dp))

                AuthInput(
                    value = oldPassword,
                    onValueChange = onOldPasswordChange,
                    label = "Current Password",
                    icon = Icons.Default.Lock,
                    isPassword = true,
                    isDark = isSystemInDarkTheme()
                )

                Spacer(Modifier.height(16.dp))

                AuthInput(
                    value = newPassword,
                    onValueChange = onNewPasswordChange,
                    label = "New Password",
                    icon = Icons.Default.VpnKey,
                    isPassword = true,
                    isDark = isSystemInDarkTheme()
                )

                AnimatedVisibility(visible = errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = appColors.trendRed,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }
            }
        }
    )
}

@Composable
fun UserProfileCard(email: String, isDark: Boolean) {
    val userInitial = email.firstOrNull()?.uppercase() ?: "A"
    val displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(32.dp), spotColor = BrandPurple.copy(alpha = 0.3f))
            .glassCard(isDark, RoundedCornerShape(32.dp))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(BrandPurple.copy(alpha = 0.15f))
                    .border(1.dp, BrandPurple.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = userInitial,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = BrandPurple
                )
            }

            Spacer(Modifier.width(20.dp))

            Column {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = if (isDark) Color.White else Color.Black
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Spacer(Modifier.height(10.dp))
                Surface(
                    color = BrandPurple,
                    shape = RoundedCornerShape(30.dp)
                ) {
                    Text(
                        text = "PRO PLATINUM",
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.5.sp
=======
                    .padding(horizontal = 16.dp)
            ) {
                // --- 1. PROFILE CARD ---
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (user?.photoUrl != null) {
                            AsyncImage(
                                model = user.photoUrl,
                                contentDescription = "Profile",
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, Color.White, CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user?.displayName?.take(1) ?: "U",
                                    fontSize = 32.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(Modifier.width(20.dp))

                        Column {
                            Text(
                                text = user?.displayName ?: "Investor",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = user?.email ?: "No Email",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Spacer(Modifier.height(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "Pro Member",
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))

                // --- 2. PREFERENCES SECTION ---
                SectionHeader("Preferences")

                // Currency Toggle
                SettingsTile(
                    icon = Icons.Default.AttachMoney,
                    title = "Display Currency",
                    subtitle = if (state.isUsd) "USD ($)" else "INR (₹)",
                    onClick = { portfolioViewModel.toggleCurrency() },
                    trailing = {
                        Switch(
                            checked = state.isUsd,
                            onCheckedChange = { portfolioViewModel.toggleCurrency() }
                        )
                    }
                )

                Spacer(Modifier.height(12.dp))

                // Notifications (UPDATED: Connected to ViewModel)
                SettingsTile(
                    icon = Icons.Default.Notifications,
                    title = "Price Alerts",
                    subtitle = if (notificationsEnabled) "On" else "Off",
                    onClick = { portfolioViewModel.toggleNotifications() },
                    trailing = {
                        Switch(
                            checked = notificationsEnabled,
                            onCheckedChange = { portfolioViewModel.toggleNotifications() }
                        )
                    }
                )

                Spacer(Modifier.height(24.dp))

                // --- 3. DATA & ACCOUNT SECTION ---
                SectionHeader("Data & Account")

                // Export CSV (Your Logic)
                SettingsTile(
                    icon = Icons.Default.FileDownload,
                    title = "Export Data",
                    subtitle = if (isPriceUpdating) "Updating..." else "Save as CSV",
                    onClick = onExportClick,
                    trailing = {
                        if (isPriceUpdating) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                Icons.Default.Download,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                )

                Spacer(Modifier.height(12.dp))

                // Sign Out
                SettingsTile(
                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                    title = "Sign Out",
                    subtitle = "Log out of your account",
                    iconTint = MaterialTheme.colorScheme.error,
                    textColor = MaterialTheme.colorScheme.error,
                    onClick = { authViewModel.signOut() }
                )

                Spacer(Modifier.height(48.dp))

                // Footer
                Box(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "ApexInvest v1.0.0",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
                    )
                }
            }
        }
    }
}
<<<<<<< HEAD

@Composable
fun SettingsGroupCard(isDark: Boolean, content: @Composable ColumnScope.() -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .glassCard(isDark, RoundedCornerShape(30.dp))
    ) {
        Column(modifier = Modifier.padding(vertical = 8.dp)) {
            content()
        }
    }
=======
@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp, start = 8.dp)
    )
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
}

@Composable
fun SettingsTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
<<<<<<< HEAD
    iconTint: Color = BrandPurple,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(iconTint.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = iconTint, modifier = Modifier.size(22.dp))
        }

        Spacer(Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray
            )
        }

        if (trailing != null) {
            trailing()
        } else {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                null,
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Black,
        color = BrandPurple,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(bottom = 12.dp, start = 16.dp)
    )
}

@Composable
fun DividerLine(isDark: Boolean) {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 20.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if(isDark) 0.15f else 0.2f)
    )
}
=======
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    textColor: Color = MaterialTheme.colorScheme.onSurface,
    trailing: @Composable (() -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint)
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = textColor)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }

            if (trailing != null) {
                trailing()
            } else {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.Gray, modifier = Modifier.size(20.dp))
            }
        }
    }
}
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
