package ke.ac.ku.ledgerly.auth.presentation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient
import ke.ac.ku.ledgerly.auth.domain.AuthEvent
import kotlinx.coroutines.delay

private val LedgerlyGreen = Color(0xFF0B3D2E)
private val LedgerlyGreenLight = Color(0xFF1A5C47)
private val LedgerlyAccent = Color(0xFFE6F0EC)
private val LedgerlyBlue = Color(0xFF4A90E2)
private val LedgerlyBlueLight = Color(0xFFD6E4F0)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    oneTapClient: SignInClient,
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var showContent by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    // Animation trigger
    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val oneTapClient = Identity.getSignInClient(context)
            val credential = oneTapClient.getSignInCredentialFromIntent(result.data)
            credential.googleIdToken?.let { viewModel.handleGoogleSignInResult(it) }
        }
    }

    LaunchedEffect(state.isAuthenticated) {
        if (state.isAuthenticated) {
            onAuthSuccess()
        }
    }

    // Handle errors with Snackbar
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            snackbarHostState.showSnackbar(
                message = error,
                duration = SnackbarDuration.Short
            )
            viewModel.onEvent(AuthEvent.DismissError)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        LedgerlyGreen,
                        LedgerlyGreenLight,
                        Color(0xFF0D4A38)
                    )
                )
            )
    ) {
        // Background decorative elements
        AnimatedVisibility(
            visible = showContent,
            enter = fadeIn(animationSpec = tween(800))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                LedgerlyBlue.copy(alpha = 0.1f),
                                Color.Transparent
                            ),
                            radius = 1000f
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Logo and tagline section
            AnimatedVisibility(
                visible = showContent,
                enter = slideInVertically(
                    initialOffsetY = { -100 },
                    animationSpec = tween(600, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(600))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AccountBalance,
                        contentDescription = "Ledgerly Logo",
                        tint = LedgerlyAccent,
                        modifier = Modifier.size(72.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Ledgerly",
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Know Your Money",
                        style = MaterialTheme.typography.titleMedium,
                        color = LedgerlyAccent.copy(alpha = 0.9f),
                        fontWeight = FontWeight.Normal
                    )
                }
            }

            // Main auth card
            AnimatedVisibility(
                visible = showContent,
                enter = slideInVertically(
                    initialOffsetY = { 100 },
                    animationSpec = tween(600, delayMillis = 200, easing = EaseOutCubic)
                ) + fadeIn(animationSpec = tween(600, delayMillis = 200))
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    elevation = CardDefaults.cardElevation(
                        defaultElevation = 8.dp
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isSignUp) "Create Account" else "Welcome Back",
                            style = MaterialTheme.typography.headlineSmall,
                            color = LedgerlyGreen,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (isSignUp)
                                "Start managing your finances today"
                            else
                                "Sign in to continue",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Email field
                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Email,
                                    contentDescription = "Email"
                                )
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            ),
                            keyboardActions = KeyboardActions(
                                onNext = { focusManager.moveFocus(FocusDirection.Down) }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LedgerlyGreen,
                                focusedLabelColor = LedgerlyGreen,
                                cursorColor = LedgerlyGreen
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Password field
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Outlined.Lock,
                                    contentDescription = "Password"
                                )
                            },
                            trailingIcon = {
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(
                                        imageVector = if (passwordVisible)
                                            Icons.Outlined.Visibility
                                        else
                                            Icons.Outlined.VisibilityOff,
                                        contentDescription = if (passwordVisible)
                                            "Hide password"
                                        else
                                            "Show password"
                                    )
                                }
                            },
                            visualTransformation = if (passwordVisible)
                                VisualTransformation.None
                            else
                                PasswordVisualTransformation(),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    focusManager.clearFocus()
                                    val event = if (isSignUp) {
                                        AuthEvent.EmailSignUp(email, password)
                                    } else {
                                        AuthEvent.EmailSignIn(email, password)
                                    }
                                    viewModel.onEvent(event)
                                }
                            ),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = LedgerlyGreen,
                                focusedLabelColor = LedgerlyGreen,
                                cursorColor = LedgerlyGreen
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (!isSignUp) {
                            TextButton(
                                onClick = { /* Handle forgot password */ },
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text(
                                    text = "Forgot password?",
                                    color = LedgerlyBlue,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        // Primary action button
                        Button(
                            onClick = {
                                focusManager.clearFocus()
                                val event = if (isSignUp) {
                                    AuthEvent.EmailSignUp(email, password)
                                } else {
                                    AuthEvent.EmailSignIn(email, password)
                                }
                                viewModel.onEvent(event)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LedgerlyGreen
                            ),
                            enabled = !state.isLoading
                        ) {
                            if (state.isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = if (isSignUp) "Create Account" else "Sign In",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Toggle sign up/sign in
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isSignUp)
                                    "Already have an account?"
                                else
                                    "Don't have an account?",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                            TextButton(onClick = { isSignUp = !isSignUp }) {
                                Text(
                                    text = if (isSignUp) "Sign In" else "Sign Up",
                                    color = LedgerlyGreen,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Divider
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f))
                            Text(
                                text = "OR",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f))
                        }

                        // Social auth buttons
                        AuthButton(
                            text = "Continue with Google",
                            icon = Icons.Outlined.AccountCircle,
                            onClick = { viewModel.onEvent(AuthEvent.GoogleSignIn) },
                            backgroundColor = Color.White,
                            contentColor = Color.DarkGray,
                            enabled = !state.isLoading
                        )

                        if (state.isBiometricAvailable) {
                            Spacer(modifier = Modifier.height(12.dp))
                            AuthButton(
                                text = "Use Biometric",
                                icon = Icons.Outlined.Fingerprint,
                                onClick = {
                                    val activity = context as FragmentActivity
                                    val executor = ContextCompat.getMainExecutor(activity)
                                    val biometricPrompt = BiometricPrompt(
                                        activity,
                                        executor,
                                        object : BiometricPrompt.AuthenticationCallback() {
                                            override fun onAuthenticationSucceeded(
                                                result: BiometricPrompt.AuthenticationResult
                                            ) {
                                                super.onAuthenticationSucceeded(result)
                                                viewModel.onBiometricSuccess()
                                            }

                                            override fun onAuthenticationError(
                                                errorCode: Int,
                                                errString: CharSequence
                                            ) {
                                                super.onAuthenticationError(errorCode, errString)
                                                viewModel.onBiometricError(errString.toString())
                                            }
                                        }
                                    )

                                    val promptInfo = BiometricPrompt.PromptInfo.Builder()
                                        .setTitle("Biometric Authentication")
                                        .setSubtitle("Sign in using your biometric credential")
                                        .setNegativeButtonText("Cancel")
                                        .build()

                                    biometricPrompt.authenticate(promptInfo)
                                },
                                backgroundColor = LedgerlyBlueLight,
                                contentColor = LedgerlyBlue,
                                enabled = !state.isLoading
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Privacy text
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn(animationSpec = tween(800, delayMillis = 400))
            ) {
                Text(
                    text = "By continuing, you agree to our Terms of Service\nand Privacy Policy",
                    style = MaterialTheme.typography.bodySmall,
                    color = LedgerlyAccent.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }

        // Snackbar host
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }
}

@Composable
private fun AuthButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    backgroundColor: Color,
    contentColor: Color,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        border = ButtonDefaults.outlinedButtonBorder.copy(
            width = 1.dp,
            brush = SolidColor(Color.LightGray)
        ),
        enabled = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}