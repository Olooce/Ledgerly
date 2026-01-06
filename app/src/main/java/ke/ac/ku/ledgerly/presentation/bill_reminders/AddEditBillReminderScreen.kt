package ke.ac.ku.ledgerly.presentation.bill_reminders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ke.ac.ku.ledgerly.R
import ke.ac.ku.ledgerly.base.BillReminderNavigationEvent
import ke.ac.ku.ledgerly.ui.theme.Typography
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBillReminderScreen(
    navController: NavController,
    billId: Long?,
    viewModel: BillReminderViewModel = hiltViewModel()
) {
    val state by viewModel.addEditState.collectAsState()

    LaunchedEffect(billId) {
        viewModel.initializeAddEdit(billId)
    }

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                BillReminderNavigationEvent.NavigateBack -> navController.popBackStack()
                else -> {}
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            if (state.isEditing) "Edit Bill Reminder" else "Add Bill Reminder",
                            style = Typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    // Bill Name
                    FormTextField(
                        label = "Bill Name",
                        value = state.billName,
                        onValueChange = { viewModel.updateBillName(it) },
                        placeholder = "e.g., Electricity Bill"
                    )
                }

                item {
                    // Description
                    FormTextField(
                        label = "Description (Optional)",
                        value = state.description,
                        onValueChange = { viewModel.updateDescription(it) },
                        placeholder = "Add details",
                        maxLines = 2
                    )
                }

                item {
                    // Amount and Currency
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = if (state.amount == 0.0) "" else state.amount.toString(),
                            onValueChange = {
                                val amount = it.toDoubleOrNull() ?: 0.0
                                viewModel.updateAmount(amount)
                            },
                            label = { Text("Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true
                        )

                        Box(
                            modifier = Modifier
                                .weight(0.5f)
                                .height(56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                state.currency,
                                style = Typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                item {
                    // Due Date
                    DueDateSelector(
                        dueDate = state.dueDate,
                        onDateChange = { viewModel.updateDueDate(it) }
                    )
                }

                item {
                    // Category
                    CategorySelector(
                        selectedCategory = state.category,
                        onCategorySelected = { viewModel.updateCategory(it) }
                    )
                }

                item {
                    // Frequency
                    FrequencySelector(
                        selectedFrequency = state.frequency,
                        onFrequencySelected = { viewModel.updateFrequency(it) }
                    )
                }

                item {
                    // Reminder Settings
                    ReminderSettingsSection(
                        reminderDays = state.reminderDays,
                        reminderEnabled = state.reminderEnabled,
                        onReminderDaysChanged = { viewModel.updateReminderDays(it) },
                        onReminderEnabledChanged = { viewModel.updateReminderEnabled(it) }
                    )
                }

                item {
                    // Payment Method
                    FormTextField(
                        label = "Payment Method (Optional)",
                        value = state.paymentMethod,
                        onValueChange = { viewModel.updatePaymentMethod(it) },
                        placeholder = "e.g., Bank Transfer"
                    )
                }

                item {
                    // Notes
                    FormTextField(
                        label = "Notes (Optional)",
                        value = state.notes,
                        onValueChange = { viewModel.updateNotes(it) },
                        placeholder = "Add any additional notes",
                        maxLines = 3
                    )
                }

                item {
                    // Color Picker
                    ColorPickerSection(
                        selectedColor = state.color,
                        onColorSelected = { viewModel.updateColor(it.toInt()) }
                    )
                }

                item {
                    // Error Message
                    if (state.error != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = Color(0xFFEF4444).copy(alpha = 0.12f)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                state.error!!,
                                modifier = Modifier.padding(12.dp),
                                color = Color(0xFFEF4444),
                                style = Typography.labelSmall
                            )
                        }
                    }
                }

                item {
                    // Save Button
                    Button(
                        onClick = { viewModel.saveBillReminder() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        enabled = !state.isLoading
                    ) {
                        if (state.isLoading) {
                            Text("Saving...", style = Typography.labelLarge)
                        } else {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.size(8.dp))
                            Text(
                                "Save Bill Reminder",
                                style = Typography.labelLarge,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun FormTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    maxLines: Int = 1,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        maxLines = maxLines
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DueDateSelector(
    dueDate: Long,
    onDateChange: (Long) -> Unit
) {
    val dateFormat = remember {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    }

    var showDatePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = dueDate
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                Button(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            onDateChange(it)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                Button(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Due Date",
                    style = Typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    dateFormat.format(Date(dueDate)),
                    style = Typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Icon(
                painter = painterResource(id = R.drawable.ic_calendar),
                contentDescription = "Calendar",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Composable
private fun CategorySelector(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        "Utilities",
        "Subscriptions",
        "Insurance",
        "Rent/Mortgage",
        "Transport",
        "Phone",
        "Internet",
        "Other"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Category", style = Typography.labelMedium, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { category ->
                CategoryChip(
                    label = category,
                    isSelected = selectedCategory == category,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.border(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            shape = RoundedCornerShape(20.dp)
        )
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = Typography.labelSmall,
            color = if (isSelected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun FrequencySelector(
    selectedFrequency: String,
    onFrequencySelected: (String) -> Unit
) {
    val frequencies = listOf("once", "monthly", "quarterly", "yearly")
    val frequencyLabels = mapOf(
        "once" to "One-time",
        "monthly" to "Monthly",
        "quarterly" to "Quarterly",
        "yearly" to "Yearly"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Frequency", style = Typography.labelMedium, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            frequencies.forEach { frequency ->
                FrequencyChip(
                    label = frequencyLabels[frequency] ?: frequency,
                    isSelected = selectedFrequency == frequency,
                    onClick = { onFrequencySelected(frequency) }
                )
            }
        }
    }
}

@Composable
private fun FrequencyChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.border(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            shape = RoundedCornerShape(20.dp)
        )
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = Typography.labelSmall,
            color = if (isSelected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun ReminderSettingsSection(
    reminderDays: Int,
    reminderEnabled: Boolean,
    onReminderDaysChanged: (Int) -> Unit,
    onReminderEnabledChanged: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Enable Reminders",
                    style = Typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
                Switch(
                    checked = reminderEnabled,
                    onCheckedChange = onReminderEnabledChanged,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary
                    )
                )
            }

            if (reminderEnabled) {
                Text(
                    "Remind me",
                    style = Typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val reminderOptions = listOf(1, 3, 5, 7, 14)
                    reminderOptions.forEach { days ->
                        ReminderDaysChip(
                            label = "$days day${if (days > 1) "s" else ""}",
                            isSelected = reminderDays == days,
                            onClick = { onReminderDaysChanged(days) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ReminderDaysChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
        else
            Color.Transparent,
        modifier = Modifier.border(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            shape = RoundedCornerShape(20.dp)
        )
    ) {
        Text(
            label,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = Typography.labelSmall,
            color = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ColorPickerSection(
    selectedColor: Int,
    onColorSelected: (Int) -> Unit
) {
    val colors = listOf(
        0xFF6750A4.toInt(),
        0xFF03DAC6.toInt(),
        0xFF4CAF50.toInt(),
        0xFFFF6E3A.toInt(),
        0xFFEF4444.toInt(),
        0xFFFFC107.toInt(),
        0xFF00BCD4.toInt(),
        0xFF9C27B0.toInt(),
        0xFF3700B3.toInt(),
        0xFF1F1F1F.toInt()
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Bill Color", style = Typography.labelMedium, fontWeight = FontWeight.Bold)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(androidx.compose.foundation.rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            colors.forEach { color ->
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(color))
                        .border(
                            width = if (selectedColor == color) 3.dp else 1.dp,
                            color = if (selectedColor == color)
                                MaterialTheme.colorScheme.primary
                            else
                                MaterialTheme.colorScheme.outline,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onColorSelected(color) },
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedColor == color) {
                        Icon(
                            Icons.Filled.Check,
                            contentDescription = "Selected",
                            modifier = Modifier.size(20.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}
