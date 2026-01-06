package ke.ac.ku.ledgerly.presentation.debt

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ke.ac.ku.ledgerly.data.constants.NavRouts
import ke.ac.ku.ledgerly.ui.theme.Typography
import ke.ac.ku.ledgerly.utils.FormatingUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.DebtDetailScreen(
    debtId: Long?,
    navController: NavController,
    animatedVisibilityScope: AnimatedVisibilityScope,
    viewModel: DebtViewModel = hiltViewModel()
) {
    val detailState by viewModel.debtDetailState.collectAsState()
    val isDarkTheme = MaterialTheme.colorScheme.background.luminance() < 0.5f

    LaunchedEffect(debtId) {
        viewModel.loadDebtDetail(debtId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Debt Details",
                            style = Typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    if (detailState.debt != null) {
                        IconButton(
                            onClick = {
                                navController.navigate("${NavRouts.ADD_EDIT_DEBT}?debtId=${detailState.debt!!.id}")
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                viewModel.deleteDebt(detailState.debt!!.id!!)
                                navController.popBackStack()
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (detailState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(40.dp),
                        color = MaterialTheme.colorScheme.primary,
                        strokeWidth = 3.dp
                    )
                }
            }

            if (detailState.debt != null) {
                val debt = detailState.debt!!
                val isOverdue =
                    debt.dueDate < System.currentTimeMillis() && debt.status != "settled"
                val dateFormat = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
                val dueDate = dateFormat.format(Date(debt.dueDate))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .sharedBounds(
                            sharedContentState = rememberSharedContentState(key = "debt-card-${debt.id}"),
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ ->
                                tween(durationMillis = 500)
                            }
                        )
                ) {
                    // Header Card with Avatar
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (debt.debtType == "owe")
                                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.08f)
                            else
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.08f)
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // Avatar
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (debt.debtType == "owe")
                                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                                        else
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f)
                                    )
                                    .sharedElement(
                                        sharedContentState = rememberSharedContentState(key = "debt-avatar-${debt.id}"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        boundsTransform = { _, _ ->
                                            tween(durationMillis = 500)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = debt.personName.firstOrNull()?.uppercase() ?: "?",
                                    style = Typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (debt.debtType == "owe")
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.primary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Name
                            Text(
                                text = debt.personName,
                                style = Typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.sharedElement(
                                    sharedContentState = rememberSharedContentState(key = "debt-name-${debt.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    boundsTransform = { _, _ ->
                                        tween(durationMillis = 500)
                                    }
                                )
                            )

                            if (debt.description.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = debt.description,
                                    style = Typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.sharedElement(
                                        sharedContentState = rememberSharedContentState(key = "debt-desc-${debt.id}"),
                                        animatedVisibilityScope = animatedVisibilityScope,
                                        boundsTransform = { _, _ ->
                                            tween(durationMillis = 500)
                                        }
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Amount
                            Text(
                                text = FormatingUtils.formatCurrency(debt.amount),
                                style = Typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (debt.debtType == "owe")
                                    MaterialTheme.colorScheme.error
                                else
                                    MaterialTheme.colorScheme.primary,
                                modifier = Modifier.sharedElement(
                                    sharedContentState = rememberSharedContentState(key = "debt-amount-${debt.id}"),
                                    animatedVisibilityScope = animatedVisibilityScope,
                                    boundsTransform = { _, _ ->
                                        tween(durationMillis = 500)
                                    }
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Badges Row
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Type Badge
                                Badge(
                                    containerColor = if (debt.debtType == "owe")
                                        MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f)
                                    else
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f),
                                    contentColor = if (debt.debtType == "owe")
                                        MaterialTheme.colorScheme.error
                                    else
                                        MaterialTheme.colorScheme.primary
                                ) {
                                    Text(
                                        text = if (debt.debtType == "owe") "I OWE" else "OWED TO ME",
                                        style = Typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                    )
                                }

                                // Overdue Badge
                                if (isOverdue) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.error,
                                        contentColor = MaterialTheme.colorScheme.onError
                                    ) {
                                        Text(
                                            text = "OVERDUE",
                                            style = Typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Details Section
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailCard(
                            title = "Due Date",
                            value = dueDate,
                            isDarkTheme = isDarkTheme,
                            isError = isOverdue
                        )

                        DetailCard(
                            title = "Status",
                            value = debt.status.replaceFirstChar { it.uppercase() },
                            isDarkTheme = isDarkTheme
                        )

                        if (debt.reminderEnabled) {
                            DetailCard(
                                title = "Reminder",
                                value = "${debt.reminderDays} day${if (debt.reminderDays != 1) "s" else ""} before due date",
                                isDarkTheme = isDarkTheme
                            )
                        }

                        if (debt.notes.isNotEmpty()) {
                            DetailCard(
                                title = "Notes",
                                value = debt.notes,
                                isDarkTheme = isDarkTheme,
                                isMultiline = true
                            )
                        }
                    }

                    // Action Button
                    if (debt.status != "settled") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                debt.id?.let { id ->
                                    viewModel.markDebtAsSettled(id)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .padding(horizontal = 16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (debt.debtType == "owe")
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            Text(
                                "Mark as Settled",
                                style = Typography.titleMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(80.dp))
                }
            } else if (detailState.error != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = detailState.error ?: "Error loading debt",
                            textAlign = TextAlign.Center,
                            style = Typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { navController.popBackStack() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("Go Back")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailCard(
    title: String,
    value: String,
    isDarkTheme: Boolean,
    isError: Boolean = false,
    isMultiline: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = title,
                style = Typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = if (isMultiline) Typography.bodyMedium else Typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = if (isError)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}