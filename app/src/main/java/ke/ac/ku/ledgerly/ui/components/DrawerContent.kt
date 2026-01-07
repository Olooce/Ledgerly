package ke.ac.ku.ledgerly.ui.components

import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ke.ac.ku.ledgerly.R
import ke.ac.ku.ledgerly.base.AuthEvent
import ke.ac.ku.ledgerly.data.constants.NavRouts
import ke.ac.ku.ledgerly.data.model.TransactionEntity
import ke.ac.ku.ledgerly.presentation.auth.AuthViewModel
import ke.ac.ku.ledgerly.presentation.transactions.ExportViewModel
import ke.ac.ku.ledgerly.ui.theme.ThemeViewModel

@Composable
fun DrawerContent(
    navController: NavController,
    themeViewModel: ThemeViewModel,
    authViewModel: AuthViewModel,
    onCloseDrawer: () -> Unit,
    transactionData: List<TransactionEntity> = emptyList(),
    drawerViewModel: DrawerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val isDarkMode by themeViewModel.isDarkMode.collectAsState()
    val userName by drawerViewModel.userName.collectAsState()
    val unreadCount by drawerViewModel.unreadCount.collectAsState()
    val exportViewModel: ExportViewModel = hiltViewModel()
    val exportState by exportViewModel.exportState.collectAsState()
    val authState by authViewModel.state.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }
    var showExportSuccessDialog by remember { mutableStateOf(false) }

    LaunchedEffect(exportState.successMessage) {
        if (exportState.successMessage != null && exportState.exportedFile != null) {
            showExportSuccessDialog = true
        }
    }

    Column(
        modifier = Modifier
            .width(320.dp)
            .fillMaxHeight()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    )
    {
        // User Profile
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onCloseDrawer()
                        navController.navigate(NavRouts.profile)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName
                            .firstOrNull()
                            ?.uppercaseChar()
                            ?.toString() ?: "U",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = userName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View & edit profile",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    painter = painterResource(id = R.drawable.ic_chevron_right),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        onCloseDrawer()
                        navController.navigate(NavRouts.notifications)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_notification),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Notifications",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )

                if (unreadCount > 0) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.error)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (unreadCount > 99) "99+" else unreadCount.toString(),
                            color = MaterialTheme.colorScheme.onError,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // App Info & Menu Items
        Column(
            modifier = Modifier.padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // App Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_ledgerly),
                    contentDescription = "Ledgerly Logo",
                    modifier = Modifier.size(72.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Ledgerly",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Know your money",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider()

            // Theme Switch
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (isDarkMode == true) R.drawable.ic_light_mode else R.drawable.ic_dark_mode
                            ),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Dark Mode",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Switch(
                    checked = isDarkMode ?: false,
                    onCheckedChange = { themeViewModel.toggleTheme() }
                )
            }

            HorizontalDivider()

            // Menu Items
            DrawerMenuItem(
                icon = R.drawable.ic_default_category,
                title = "Debt Tracker",
                onClick = {
                    onCloseDrawer()
                    navController.navigate(NavRouts.debtTracker)
                }
            )

            HorizontalDivider()

            DrawerMenuItem(
                icon = R.drawable.ic_default_category,
                title = "Savings Goals",
                onClick = {
                    onCloseDrawer()
                    navController.navigate(NavRouts.savingsGoals)
                }
            )

            HorizontalDivider()

            DrawerMenuItem(
                icon = R.drawable.ic_reminder,
                title = "Bill Reminders",
                onClick = {
                    onCloseDrawer()
                    navController.navigate(NavRouts.billReminders)
                }
            )

            HorizontalDivider()

            DrawerMenuItem(
                icon = R.drawable.ic_default_category,
                title = "Categories",
                onClick = {
                    onCloseDrawer()
                    navController.navigate(NavRouts.categoryManagement)
                }
            )

            HorizontalDivider()

            DrawerMenuItem(
                icon = R.drawable.ic_settings,
                title = "Settings",
                onClick = {
                    onCloseDrawer()
                    navController.navigate(NavRouts.settings)
                }
            )

            HorizontalDivider()

            DrawerMenuItem(
                icon = R.drawable.ic_export,
                title = "Export Data",
                onClick = { showExportDialog = true }
            )

            HorizontalDivider()

            // Version Info
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        // Bottom Section - Footer & Logout
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "GitHub",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable {
                        val githubIntent = Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/Olooce".toUri()
                        )
                        context.startActivity(githubIntent)
                    }
                )
                Text(
                    text = "LinkedIn",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.clickable {
                        val linkedinIntent = Intent(
                            Intent.ACTION_VIEW,
                            "https://www.linkedin.com/in/oloo-stephen/".toUri()
                        )
                        context.startActivity(linkedinIntent)
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    authViewModel.onEvent(AuthEvent.SignOut)
                    onCloseDrawer()

                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !authState.isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                if (authState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                } else {
                    Text("Logout")
                }
            }
        }
    }

    // Export Dialog
    if (showExportDialog) {
        ExportDialog(
            transactions = transactionData,
            exportViewModel = exportViewModel,
            onDismiss = { showExportDialog = false }
        )
    }

    // Export Status Snackbar
    val exportMessage = exportState.successMessage ?: exportState.errorMessage
    ExportStatusSnackbar(
        message = exportMessage,
        isError = exportState.errorMessage != null,
        onDismiss = { exportViewModel.clearMessages() }
    )

    // Export Progress Dialog
    ExportProgressDialog(
        progress = exportState.exportProgress,
        isExporting = exportState.isExporting
    )

    // Export Success Dialog
    if (showExportSuccessDialog && exportState.exportedFile != null) {
        ExportSuccessDialog(
            exportViewModel = exportViewModel,
            file = exportState.exportedFile,
            onDismiss = { showExportSuccessDialog = false }
        )
    }
}

@Composable
private fun DrawerMenuItem(
    icon: Int,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}