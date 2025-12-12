package ke.ac.ku.ledgerly.presentation.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Fingerprint
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import ke.ac.ku.ledgerly.base.AuthEvent

private val AccentColor = Color(0xFF06B6D4)       // Cyan accent
private val PrimaryGradient1 = Color(0xFF155E75)  // Deep ocean blue
private val PrimaryGradient2 = Color(0xFF0F766E)  // Teal
private val PrimaryGradient3 = Color(0xFF164E63)  // Dark cyan
private val SurfaceLight = Color(0xFFFFFFFF)
private val TextSecondary = Color(0xFF64748B)
private val SuccessColor = Color(0xFF10B981)

/**
 * BiometricOptInScreen allows users to enable biometric unlock after successful login.
 *
 */
@Composable
fun BiometricOptInScreen(
    onContinue: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.state.collectAsState()
    var isBiometricEnabled by remember { mutableStateOf(authState.isBiometricEnabled) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = listOf(
                        PrimaryGradient1,
                        PrimaryGradient2,
                        PrimaryGradient3,
                    ),
                    start = androidx.compose.ui.geometry.Offset(0f, 0f),
                    end = androidx.compose.ui.geometry.Offset(1000f, 1000f)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Header with icon
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(initialOffsetY = { -100 }) + fadeIn()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(
                                color = SurfaceLight.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(24.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Fingerprint,
                            contentDescription = "Biometric",
                            modifier = Modifier.size(56.dp),
                            tint = AccentColor
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Quick & Secure Access",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = SurfaceLight
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Unlock Ledgerly with your fingerprint or face",
                        style = MaterialTheme.typography.bodyLarge,
                        color = SurfaceLight.copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Benefits section
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(initialOffsetY = { 50 }) + fadeIn()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    // Benefits Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = SurfaceLight.copy(alpha = 0.95f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            BenefitItem(
                                title = "Quick Access",
                                description = "No need to type your password every time"
                            )

                            BenefitItem(
                                title = "Secure",
                                description = "Biometric data never leaves your device"
                            )

                            BenefitItem(
                                title = "Session Protection",
                                description = "Biometric re-authentication when needed"
                            )

                            BenefitItem(
                                title = "Device-Locked",
                                description = "Works only on your device with your credentials"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Toggle Section
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isBiometricEnabled)
                                AccentColor.copy(alpha = 0.12f)
                            else
                                SurfaceLight.copy(alpha = 0.95f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = "Enable Biometric Unlock",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (isBiometricEnabled) AccentColor else androidx.compose.ui.graphics.Color(
                                        0xFF1E293B
                                    )
                                )
                                Text(
                                    text = if (isBiometricEnabled)
                                        "Enabled - Tap to disable"
                                    else
                                        "Disabled - Tap to enable",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }

                            Switch(
                                checked = isBiometricEnabled,
                                onCheckedChange = { isChecked ->
                                    isBiometricEnabled = isChecked
                                    viewModel.onEvent(AuthEvent.EnableBiometricUnlock(isChecked))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = AccentColor,
                                    checkedTrackColor = AccentColor.copy(alpha = 0.4f),
                                    uncheckedThumbColor = TextSecondary,
                                    uncheckedTrackColor = TextSecondary.copy(alpha = 0.3f)
                                )
                            )
                        }
                    }

                    // Additional info
                    if (isBiometricEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = SuccessColor.copy(alpha = 0.12f)
                            ),
                            border = BorderStroke(1.5.dp, SuccessColor.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.CheckCircle,
                                    contentDescription = "Info",
                                    modifier = Modifier.size(22.dp),
                                    tint = SuccessColor
                                )
                                Text(
                                    text = "You can change this setting anytime in the app settings.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SuccessColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(initialOffsetY = { 100 }) + fadeIn()
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = {
                            // Ensure setting is saved before continuing
                            viewModel.onEvent(AuthEvent.EnableBiometricUnlock(isBiometricEnabled))
                            viewModel.dismissBiometricOptIn()
                            onContinue()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentColor
                        ),
                        enabled = !authState.isLoading
                    ) {
                        Text(
                            text = "Continue",
                            fontWeight = FontWeight.Bold,
                            color = SurfaceLight,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = {
                            // Don't enable, just continue
                            viewModel.dismissBiometricOptIn()
                            onContinue()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "Maybe Later",
                            color = AccentColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
private fun BenefitItem(
    title: String,
    description: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Outlined.CheckCircle,
            contentDescription = "Benefit",
            modifier = Modifier.size(24.dp),
            tint = SuccessColor
        )
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E293B)
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                modifier = Modifier.padding(top = 3.dp)
            )
        }
    }
}
