package ke.ac.ku.ledgerly.presentation.savings_goals

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ke.ac.ku.ledgerly.R
import ke.ac.ku.ledgerly.data.model.SavingsGoalEntity
import ke.ac.ku.ledgerly.ui.components.rememberSafeIconPainter
import ke.ac.ku.ledgerly.ui.theme.LedgerlyGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingsGoalScreen(
    navController: NavController,
    goalId: Long? = null,
    viewModel: SavingsGoalViewModel = hiltViewModel()
) {
    val selectedGoal by viewModel.selectedGoal.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var goalName by remember { mutableStateOf("") }
    var goalDescription by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var currentAmount by remember { mutableStateOf("") }
    var goalIcon by remember { mutableStateOf(R.drawable.ic_target) }
    var goalColor by remember { mutableStateOf("#4CAF50") }

    val ICON_OPTIONS = listOf(
        R.drawable.ic_target,
        R.drawable.ic_goal_house,
        R.drawable.ic_goal_car,
        R.drawable.ic_goal_laptop,
        R.drawable.ic_goal_plane,
        R.drawable.ic_goal_family,
        R.drawable.ic_goal_school,
//        R.drawable.ic_goal_ring,
//        R.drawable.ic_goal_watch,
//        R.drawable.ic_goal_game
    )

    val COLOR_OPTIONS = listOf(
        "#4CAF50", "#2196F3", "#FF9800", "#E91E63",
        "#9C27B0", "#00BCD4", "#F44336", "#FFC107"
    )

    LaunchedEffect(goalId) {
        if (goalId != null && goalId > 0) {
            viewModel.selectGoal(goalId)
        }
    }

    LaunchedEffect(selectedGoal) {
        selectedGoal?.let { goal ->
            goalName = goal.name
            goalDescription = goal.description
            targetAmount = goal.targetAmount.toString()
            currentAmount = goal.currentAmount.toString()
            goalIcon = goal.icon
            goalColor = goal.color
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (goalId != null && goalId > 0) "Edit Goal" else "New Goal",
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (goalName.isNotEmpty() && targetAmount.toDoubleOrNull() != null && targetAmount.toDoubleOrNull()!! > 0) {
                ExtendedFloatingActionButton(
                    onClick = {
                        val target = targetAmount.toDoubleOrNull() ?: 0.0
                        val current = currentAmount.toDoubleOrNull() ?: 0.0

                        val isEdit = goalId != null && goalId > 0
                        val original = selectedGoal

                        val goal = SavingsGoalEntity(
                            id = if (isEdit) goalId else 0L,
                            name = goalName,
                            description = goalDescription,
                            targetAmount = target,
                            currentAmount = current,
                            icon = goalIcon,
                            color = goalColor,
                            targetDate = original?.targetDate,
                            createdDate = original?.createdDate ?: System.currentTimeMillis(),
                            lastModified = System.currentTimeMillis()
                        )

                        if (isEdit) {
                            viewModel.updateGoal(goal)
                        } else {
                            viewModel.addGoal(goal)
                        }

                        navController.popBackStack()
                    },
                    modifier = Modifier.padding(bottom = 16.dp, end = 16.dp),
                    containerColor = LedgerlyGreen,
                    contentColor = Color.White
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_save),
                        contentDescription = "Save",
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (goalId != null && goalId > 0) "Update" else "Save",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Icon Selection
            Text(
                "Icon",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp, top = 12.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                ICON_OPTIONS.forEach { iconRes ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { goalIcon = iconRes }
                            .background(
                                if (goalIcon == iconRes)
                                    LedgerlyGreen.copy(alpha = 0.12f)
                                else
                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            )
                            .border(
                                width = if (goalIcon == iconRes) 2.dp else 1.dp,
                                color = if (goalIcon == iconRes)
                                    LedgerlyGreen
                                else
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {

                        val iconPainter = rememberSafeIconPainter(iconRes, R.drawable.ic_target)
                        Icon(
                            painter = iconPainter,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }


            // Color Selection
            Text(
                "Color",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                COLOR_OPTIONS.forEach { colorHex ->
                    val color = Color(colorHex.toColorInt())

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { goalColor = colorHex }
                            .background(color)
                            .border(
                                width = if (goalColor == colorHex) 2.dp else 0.dp,
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (goalColor == colorHex) {
                            Icon(
                                painter = painterResource(R.drawable.ic_check),
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }


            // Form Fields
            OutlinedTextField(
                value = goalName,
                onValueChange = { goalName = it },
                label = { Text("Goal Name") },
                placeholder = { Text("e.g., New Laptop") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                singleLine = true
            )

            OutlinedTextField(
                value = goalDescription,
                onValueChange = { goalDescription = it },
                label = { Text("Description (Optional)") },
                placeholder = { Text("Add details...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                maxLines = 2
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = { targetAmount = it },
                    label = { Text("Target") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )

                OutlinedTextField(
                    value = currentAmount,
                    onValueChange = { currentAmount = it },
                    label = { Text("Saved") },
                    placeholder = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(Modifier.height(80.dp))
        }
    }
}