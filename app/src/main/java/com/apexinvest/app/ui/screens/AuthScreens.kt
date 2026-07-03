package com.apexinvest.app.ui.screens

<<<<<<< HEAD
import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apexinvest.app.R
import com.apexinvest.app.ui.theme.BrandPurple
import com.apexinvest.app.ui.theme.LocalAppColors
import com.apexinvest.app.viewmodel.AuthState
import com.apexinvest.app.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

enum class ScreenStep {
    LOGIN, SIGNUP, FORGOT_PASSWORD_REQUEST, VERIFY_REGISTRATION_OTP, VERIFY_RESET_OTP
}

@Composable
fun AuthScreen(
    initialIsLogin: Boolean,
    onNavigateToSignUp: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    authViewModel: AuthViewModel,
    isConnected: Boolean = true
) {
    val view = LocalView.current
    val authState by authViewModel.authState.collectAsStateWithLifecycle()
    val appColors = LocalAppColors.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var emailError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var otpError by remember { mutableStateOf(false) }
    var newPasswordError by remember { mutableStateOf(false) }

    var timerSeconds by remember { mutableIntStateOf(60) }
    var isTimerRunning by remember { mutableStateOf(false) }

    var currentStep by remember {
        mutableStateOf(if (initialIsLogin) ScreenStep.LOGIN else ScreenStep.SIGNUP)
    }

    val isSubFlow = currentStep != ScreenStep.LOGIN && currentStep != ScreenStep.SIGNUP
    val isDark = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    val meshBrush = remember(isDark) {
        Brush.verticalGradient(
            listOf(BrandPurple.copy(alpha = if (isDark) 0.12f else 0.05f), Color.Transparent)
        )
    }

    BackHandler(enabled = isSubFlow) {
        errorMessage = null
        authViewModel.clearError()
        currentStep = if (initialIsLogin) ScreenStep.LOGIN else ScreenStep.SIGNUP
    }

    LaunchedEffect(isTimerRunning, timerSeconds) {
        if (isTimerRunning && timerSeconds > 0) {
            delay(1000L)
            timerSeconds--
        } else if (timerSeconds == 0) {
            isTimerRunning = false
        }
    }

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.LoggedIn -> errorMessage = null
            is AuthState.OtpVerificationRequired -> {
                errorMessage = null
                currentStep = ScreenStep.VERIFY_REGISTRATION_OTP
                timerSeconds = 60
                isTimerRunning = true
            }
            is AuthState.PasswordResetRequired -> {
                errorMessage = null
                currentStep = ScreenStep.VERIFY_RESET_OTP
                timerSeconds = 60
                isTimerRunning = true
            }
            is AuthState.Error -> errorMessage = (authState as AuthState.Error).message
            is AuthState.LoggedOut -> errorMessage = null
            else -> errorMessage = null
        }
    }

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !isDark
        }
    }

    fun validateAndProceed() {
        emailError = false; passwordError = false; otpError = false; newPasswordError = false
        when (currentStep) {
            ScreenStep.LOGIN, ScreenStep.SIGNUP -> {
                if (email.isBlank() || password.isBlank()) {
                    emailError = email.isBlank(); passwordError = password.isBlank()
                    authViewModel.showLocalValidationError("Please fill in all fields.")
                } else {
                    if (currentStep == ScreenStep.LOGIN) authViewModel.signInWithEmail(email, password)
                    else authViewModel.signUpWithEmail(email, password)
                }
            }
            ScreenStep.FORGOT_PASSWORD_REQUEST -> {
                if (email.isBlank()) {
                    emailError = true
                    authViewModel.showLocalValidationError("Enter your email to reset.")
                } else authViewModel.requestPasswordReset(email)
            }
            ScreenStep.VERIFY_REGISTRATION_OTP, ScreenStep.VERIFY_RESET_OTP -> {
                if (otpCode.length < 6 || (currentStep == ScreenStep.VERIFY_RESET_OTP && newPassword.isBlank())) {
                    otpError = otpCode.length < 6
                    newPasswordError = currentStep == ScreenStep.VERIFY_RESET_OTP && newPassword.isBlank()
                    authViewModel.showLocalValidationError("Invalid entry. Please check fields.")
                } else {
                    authViewModel.submitOtp(otpCode, newPassword)
                }
            }
        }
    }

    fun handleResendOtp() {
        otpCode = ""
        timerSeconds = 60
        isTimerRunning = true
        if (currentStep == ScreenStep.VERIFY_REGISTRATION_OTP) {
            authViewModel.resendRegistrationOtp(email)
        } else {
            authViewModel.requestPasswordReset(email)
        }
    }

    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(meshBrush)
            .then(if (isConnected) Modifier.statusBarsPadding() else Modifier)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(40.dp))
            BrandLogoSection()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(Modifier.height(24.dp))
                AuthHeaderSection(currentStep)

                Spacer(Modifier.height(32.dp))
                ErrorDisplaySection(errorMessage) { authViewModel.clearError() }

                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = { (fadeIn() + slideInHorizontally { it }).togetherWith(fadeOut() + slideOutHorizontally { -it }) },
                    label = "AuthFormSwitcher"
                ) { step ->
                    Column(modifier = Modifier.fillMaxWidth()) {
                        when (step) {
                            ScreenStep.LOGIN, ScreenStep.SIGNUP -> {
                                StandardAuthForm(email, password, step == ScreenStep.LOGIN, emailError, passwordError, isDark,
                                    onEmailChange = { email = it; emailError = false },
                                    onPasswordChange = { password = it; passwordError = false },
                                    onForgotClick = {
                                        errorMessage = null
                                        authViewModel.clearError()
                                        currentStep = ScreenStep.FORGOT_PASSWORD_REQUEST
                                    }
                                )

                                Spacer(Modifier.height(24.dp))

                                AuthPrimaryButton(
                                    text = if(step == ScreenStep.LOGIN) "Sign In" else "Create Account",
                                    isLoading = authState is AuthState.Loading
                                ) { validateAndProceed() }

                                Spacer(Modifier.height(24.dp))
                                OrDivider(isDark)
                                Spacer(Modifier.height(24.dp))

                                GoogleSignInButton(isLoading = authState is AuthState.Loading, isDark = isDark) {
                                    onGoogleSignInClick()
                                }
                            }
                            ScreenStep.FORGOT_PASSWORD_REQUEST -> {
                                AuthInput(email, { email = it; emailError = false }, "Account Email", Icons.Default.Email, isError = emailError, keyboardType = KeyboardType.Email, isDark = isDark)
                                Spacer(Modifier.height(24.dp))
                                AuthPrimaryButton("Send Code", authState is AuthState.Loading) { validateAndProceed() }
                            }
                            ScreenStep.VERIFY_REGISTRATION_OTP -> {
                                AuthInput(otpCode, { if (it.length <= 6) { otpCode = it; otpError = false } }, "Verification Code", Icons.Default.Pin, isError = otpError, keyboardType = KeyboardType.Number, isDark = isDark)
                                Spacer(Modifier.height(16.dp))
                                ResendOtpSection(timerSeconds, isTimerRunning) { handleResendOtp() }
                                Spacer(Modifier.height(24.dp))
                                AuthPrimaryButton("Verify Email", authState is AuthState.Loading) { validateAndProceed() }
                            }
                            ScreenStep.VERIFY_RESET_OTP -> {
                                AuthInput(otpCode, { if (it.length <= 6) { otpCode = it; otpError = false } }, "OTP from Email", Icons.Default.Pin, isError = otpError, keyboardType = KeyboardType.Number, isDark = isDark)
                                Spacer(Modifier.height(16.dp))
                                AuthInput(newPassword, { newPassword = it; newPasswordError = false }, "New Password", Icons.Default.Lock, isError = newPasswordError, isPassword = true, isDark = isDark)
                                PasswordStrengthMeter(newPassword)
                                Spacer(Modifier.height(16.dp))
                                ResendOtpSection(timerSeconds, isTimerRunning) { handleResendOtp() }
                                Spacer(Modifier.height(24.dp))
                                AuthPrimaryButton("Reset Password", authState is AuthState.Loading) { validateAndProceed() }
                            }
                        }
                    }
                }
            }

            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (currentStep == ScreenStep.LOGIN || currentStep == ScreenStep.SIGNUP) {
                    AuthSwitchText(if (currentStep == ScreenStep.LOGIN) "New here? Create an Account" else "Already a member? Log In") {
                        errorMessage = null
                        authViewModel.clearError()
                        if (currentStep == ScreenStep.LOGIN) onNavigateToSignUp() else onNavigateToLogin()
                    }
                } else {
                    AuthSwitchText("Cancel and Go Back", isCancel = true) {
                        errorMessage = null
                        authViewModel.clearError()
                        currentStep = if (initialIsLogin) ScreenStep.LOGIN else ScreenStep.SIGNUP
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordStrengthMeter(password: String) {
    if (password.isEmpty()) return
    val appColors = LocalAppColors.current
    val strength = remember(password) {
        var score = 0
        if (password.length > 6) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++
        score
    }
    val color = when (strength) {
        0, 1 -> appColors.trendRed
        2 -> appColors.trendOrange
        3 -> appColors.trendGreen.copy(alpha = 0.8f)
        else -> appColors.trendGreen
    }
    val label = when (strength) {
        0, 1 -> "Weak"
        2 -> "Fair"
        3 -> "Good"
        else -> "Strong"
    }
    Column(modifier = Modifier.padding(top = 8.dp).fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Security", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
        }
        Spacer(Modifier.height(4.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            repeat(4) { index ->
                Box(modifier = Modifier.weight(1f).height(4.dp).clip(CircleShape).background(if (index < strength) color else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)))
            }
        }
    }
}

@Composable
fun StandardAuthForm(
    email: String, password: String, isLogin: Boolean, emailError: Boolean, passwordError: Boolean, isDark: Boolean,
    onEmailChange: (String) -> Unit, onPasswordChange: (String) -> Unit, onForgotClick: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        AuthInput(email, onEmailChange, "Email", Icons.Default.Email, isError = emailError, keyboardType = KeyboardType.Email, isDark = isDark)
        Spacer(Modifier.height(16.dp))
        AuthInput(password, onPasswordChange, "Password", Icons.Default.Lock, isError = passwordError, isPassword = true, isDark = isDark)
        if (!isLogin) PasswordStrengthMeter(password)
        else {
            TextButton(onClick = onForgotClick, modifier = Modifier.align(Alignment.End)) {
                Text("Forgot Password?", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = BrandPurple)
            }
        }
    }
}

@Composable
fun ErrorDisplaySection(message: String?, onTimeout: () -> Unit) {
    val appColors = LocalAppColors.current
    LaunchedEffect(message) {
        if (message != null) {
            delay(5000L)
            onTimeout()
        }
    }
    AnimatedVisibility(visible = message != null, enter = expandVertically(), exit = shrinkVertically()) {
        Box(
            modifier = Modifier
                .padding(bottom = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(appColors.trendRed.copy(alpha = 0.1f))
                .border(1.dp, appColors.trendRed.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
        ) {
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Info, null, tint = appColors.trendRed, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(message ?: "", color = appColors.trendRed, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun AuthPrimaryButton(text: String, isLoading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(16.dp),
        enabled = !isLoading,
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp,
            disabledElevation = 0.dp
        ),
        colors = ButtonDefaults.buttonColors(
            containerColor = BrandPurple,
            contentColor = Color.White,
            disabledContainerColor = BrandPurple.copy(alpha = 0.8f),
            disabledContentColor = Color.White.copy(alpha = 0.8f)
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp),
                strokeWidth = 3.dp
            )
        } else {
            Text(text, fontWeight = FontWeight.Black, fontSize = 16.sp, letterSpacing = 1.sp)
        }
    }
}

@Composable
fun GoogleSignInButton(isLoading: Boolean, isDark: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(
                width = 1.dp,
                color = if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.White,
            contentColor = if (isDark) Color.White else Color.Black,
            disabledContentColor = (if (isDark) Color.White else Color.Black).copy(alpha = 0.6f)
        ),
        enabled = !isLoading
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = BrandPurple,
                modifier = Modifier.size(24.dp),
                strokeWidth = 3.dp
            )
        } else {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google_logo),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text("Continue with Google", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

@Composable
fun ResendOtpSection(timerSeconds: Int, isTimerRunning: Boolean, onResendClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
        Text(text = "Didn't receive the code? ", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        if (isTimerRunning) Text(text = "Resend in ${timerSeconds}s", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BrandPurple)
        else {
            TextButton(onClick = onResendClick, contentPadding = PaddingValues(0.dp)) {
                Text(text = "Resend OTP", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = BrandPurple)
            }
        }
    }
}

@Composable
fun BrandLogoSection() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(76.dp)
                .shadow(24.dp, CircleShape, spotColor = BrandPurple.copy(alpha = 0.6f))
                .background(Brush.linearGradient(listOf(BrandPurple, Color(0xFF9575CD))), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.AutoMirrored.Filled.TrendingUp, null, modifier = Modifier.size(38.dp), tint = Color.White)
        }
        Spacer(Modifier.height(16.dp))
        Text("APEX INVEST", fontWeight = FontWeight.Black, letterSpacing = 6.sp, style = MaterialTheme.typography.titleMedium, color = BrandPurple)
    }
}

@Composable
fun AuthHeaderSection(step: ScreenStep) {
    val (title, subtitle) = when (step) {
        ScreenStep.LOGIN -> "Welcome Back" to "Sign in to manage your portfolio"
        ScreenStep.SIGNUP -> "Join ApexInvest" to "Start your investment journey"
        ScreenStep.FORGOT_PASSWORD_REQUEST -> "Reset Password" to "Enter email to receive a code"
        ScreenStep.VERIFY_REGISTRATION_OTP -> "Verify Email" to "Enter the code sent to your inbox"
        ScreenStep.VERIFY_RESET_OTP -> "New Password" to "Secure your account"
    }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black)
        Spacer(Modifier.height(6.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = BrandPurple, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
    }
}

@Composable
fun OrDivider(isDark: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if(isDark) 0.15f else 0.2f))
        Text("OR", modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
        HorizontalDivider(modifier = Modifier.weight(1f), thickness = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if(isDark) 0.15f else 0.2f))
    }
}

@Composable
fun AuthSwitchText(text: String, isCancel: Boolean = false, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Text(text, fontWeight = if (isCancel) FontWeight.Medium else FontWeight.Bold, color = if (isCancel) MaterialTheme.colorScheme.onSurfaceVariant else BrandPurple)
    }
}

@Composable
fun AuthInput(
    value: String, onValueChange: (String) -> Unit, label: String, icon: ImageVector,
    isError: Boolean = false, isPassword: Boolean = false, keyboardType: KeyboardType = KeyboardType.Text, isDark: Boolean
) {
    val appColors = LocalAppColors.current

    // Performance optimization: Hoisting the transformation logic
    val visualTransformation = remember(isPassword) {
        if (isPassword) PasswordVisualTransformation() else VisualTransformation.None
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = { Icon(icon, null, modifier = Modifier.size(20.dp)) },
        isError = isError,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = if (isDark) Color.White else Color.Black,
            unfocusedTextColor = if (isDark) Color.White else Color.Black,
            focusedContainerColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f),
            unfocusedContainerColor = if (isDark) Color.White.copy(alpha = 0.05f) else Color.Transparent,
            errorContainerColor = appColors.trendRed.copy(alpha = 0.05f),
            focusedBorderColor = BrandPurple,
            unfocusedBorderColor = if (isDark) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.12f),
            errorBorderColor = appColors.trendRed,
            focusedLeadingIconColor = BrandPurple,
            unfocusedLeadingIconColor = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.4f),
            focusedLabelColor = BrandPurple,
            unfocusedLabelColor = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Black.copy(alpha = 0.4f)
        )
    )
=======
import android.content.SharedPreferences
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.apexinvest.app.viewmodel.AuthState
import com.apexinvest.app.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

@Composable
fun AuthScreen(
    isLogin: Boolean,
    onNavigateToSignUp: () -> Unit,
    onNavigateToPhoneAuth: () -> Unit,
    authViewModel: AuthViewModel,
    webClientId: String
) {
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(webClientId)
        .requestEmail()
        .build()
    val googleSignInClient = remember { GoogleSignIn.getClient(context, gso) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            task.result.idToken?.let { authViewModel.handleGoogleSignInResult(it) }
        } catch (e: Exception) {
            Toast.makeText(context, "Google Sign-in failed", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.AutoMirrored.Filled.TrendingUp,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(24.dp))

        Text(
            if (isLogin) "Welcome Back" else "Create Account",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            leadingIcon = { Icon(Icons.Default.MailOutline, null) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            leadingIcon = { Icon(Icons.Default.Lock, null) },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (isLogin) authViewModel.signInWithEmail(email, password)
                else authViewModel.signUpWithEmail(email, password)
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text(if (isLogin) "Log In" else "Sign Up")
            }
        }

        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = {
                scope.launch {
                    googleSignInClient.signOut()
                    launcher.launch(googleSignInClient.signInIntent)
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Share, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Continue with Google")
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onNavigateToPhoneAuth) {
            Text("Sign in with Phone Number")
        }

        TextButton(onClick = onNavigateToSignUp) {
            Text(if (isLogin) "Don't have an account? Sign Up" else "Already have an account? Log In")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneAuthScreen(authViewModel: AuthViewModel, onBack: () -> Unit, prefs: SharedPreferences) {
    val activity = LocalContext.current as androidx.activity.ComponentActivity
    val context = LocalContext.current
    val authState by authViewModel.authState.collectAsState()

    var phoneNumber by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var codeSent by remember { mutableStateOf(false) }

    val callbacks = remember {
        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(c: com.google.firebase.auth.PhoneAuthCredential) {
                authViewModel.signInWithPhoneCredential(c)
            }
            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
            }
            override fun onCodeSent(id: String, token: PhoneAuthProvider.ForceResendingToken) {
                verificationId = id
                codeSent = true
                prefs.edit { putBoolean("phone_auth_in_progress", true) }
            }
        }
    }

    // --- REPLACED SCAFFOLD WITH COLUMN + HEADER ---
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Custom Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "Phone Login",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            if (!codeSent) {
                Text("Enter your phone number", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone (e.g. +91...)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        if(phoneNumber.length > 5) {
                            val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
                                .setPhoneNumber(phoneNumber)
                                .setTimeout(60L, TimeUnit.SECONDS)
                                .setActivity(activity)
                                .setCallbacks(callbacks)
                                .build()
                            PhoneAuthProvider.verifyPhoneNumber(options)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (authState is AuthState.Loading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    else Text("Get OTP Code")
                }
            } else {
                Text("Enter Verification Code", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("6-Digit OTP") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        verificationId?.let { id ->
                            val credential = PhoneAuthProvider.getCredential(id, code)
                            authViewModel.signInWithPhoneCredential(credential)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (authState is AuthState.Loading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
                    else Text("Verify & Login")
                }
            }
        }
    }
>>>>>>> cd20cf09d1884ae6ac18adf62ae1b323ea6382c2
}