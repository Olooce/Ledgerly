@file:OptIn(ExperimentalMaterial3Api::class)

package ke.ac.ku.ledgerly.presentation.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import ke.ac.ku.ledgerly.R
import ke.ac.ku.ledgerly.data.model.CategoryEntity
import ke.ac.ku.ledgerly.ui.components.ColorPickerDialog
import ke.ac.ku.ledgerly.ui.components.IconPickerDialog
import ke.ac.ku.ledgerly.ui.theme.Typography
import ke.ac.ku.ledgerly.ui.widget.TransactionTextView

@Composable
fun CategoryManagementScreen(
    navController: NavController,
    viewModel: CategoryManagementViewModel = hiltViewModel()
) {
    val allCategories by viewModel.allCategories.collectAsState()
    val customCategories by viewModel.customCategories.collectAsState()
    val defaultCategories by viewModel.defaultCategories.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val showCreateDialog by viewModel.showCreateDialog.collectAsState()
    val editingCategory by viewModel.editingCategory.collectAsState()

    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(24.dp)
                            .clickable { navController.popBackStack() }
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        "Categories",
                        style = Typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Button(onClick = { viewModel.openCreateDialog() }) {
                    Text("+ Add")
                }
            }

            // Error Message
            if (errorMessage != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        errorMessage ?: "",
                        modifier = Modifier.padding(12.dp),
                        color = MaterialTheme.colorScheme.error,
                        style = Typography.bodySmall
                    )
                }
            }

            // Loading indicator
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Loading categories...")
                }
            } else if (allCategories.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .align(Alignment.CenterHorizontally),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No categories available")
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Default Categories Section
                    if (defaultCategories.isNotEmpty()) {
                        item {
                            Text(
                                "Default Categories",
                                style = Typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(defaultCategories) { category ->
                            CategoryItemCard(
                                category = category,
                                onEdit = { viewModel.setEditingCategory(category) },
                                onColorChange = { newColor ->
                                    viewModel.updateDefaultCategoryColor(category.id, newColor)
                                },
                                canDelete = false
                            )
                        }
                    }

                    // Custom Categories Section
                    if (customCategories.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                        item {
                            Text(
                                "Custom Categories",
                                style = Typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        items(customCategories) { category ->
                            CategoryItemCard(
                                category = category,
                                onEdit = { viewModel.setEditingCategory(category) },
                                onDelete = { viewModel.deleteCategory(category.id) },
                                canDelete = true
                            )
                        }
                    }
                }
            }
        }
    }

    // Create/Edit Category Dialog
    if (showCreateDialog) {
        CreateCategoryDialog(
            onDismiss = { viewModel.closeCreateDialog() },
            onCreate = { name, icon, color, categoryType ->
                viewModel.createCategory(name, icon, color, categoryType)
            }
        )
    }

    // Edit Category Dialog
    if (editingCategory != null) {
        EditCategoryDialog(
            category = editingCategory!!,
            onDismiss = { viewModel.setEditingCategory(null) },
            onUpdate = { name, icon, color ->
                viewModel.updateCategory(editingCategory!!.id, name, icon, color)
            }
        )
    }
}

@Composable
private fun CategoryItemCard(
    category: CategoryEntity,
    onEdit: () -> Unit,
    onColorChange: (Long) -> Unit = {},
    onDelete: () -> Unit = {},
    canDelete: Boolean = false
) {
    var showColorPicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp)),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clickable { if (!category.isDefault) onEdit() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Icon Box
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            Color(category.color.toInt()),
                            RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = if (category.icon != 0) category.icon else R.drawable.ic_default_category),
                        contentDescription = "Category icon",
                        modifier = Modifier.size(24.dp),
                        tint = Color.White
                    )
                }

                // Category Details
                Column {
                    TransactionTextView(
                        text = category.name,
                        style = Typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    TransactionTextView(
                        text = if (category.isDefault) "Default" else "Custom",
                        style = Typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!category.isDefault) {
                    TextButton(onClick = onEdit) {
                        Text("Edit", fontSize = 12.sp)
                    }
                    TextButton(onClick = onDelete) {
                        Text("Delete", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
                    }
                } else {
                    TextButton(onClick = { showColorPicker = true }) {
                        Text("Color", fontSize = 12.sp)
                    }
                }
            }
        }
    }

    if (showColorPicker) {
        ColorPickerDialog(
            currentColor = Color(category.color.toInt()),
            onColorSelected = { selectedColor ->
                onColorChange(selectedColor.toArgb().toLong())
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }
}

@Composable
private fun CreateCategoryDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Int, Long, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableIntStateOf(R.drawable.ic_default_category) }
    var selectedColor by remember { mutableLongStateOf(0xFF6750A4) }
    var selectedType by remember { mutableStateOf("Expense") }
    var showColorPicker by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Create Category")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Type Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Expense", "Income").forEach { type ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selectedType == type)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.surfaceVariant
                                )
                                .clickable { selectedType = type }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                type,
                                color = if (selectedType == type)
                                    MaterialTheme.colorScheme.onPrimary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Icon Picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(
                            Color(selectedColor.toInt()),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { showIconPicker = true }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = if (selectedIcon != 0) selectedIcon else R.drawable.ic_default_category),
                        contentDescription = "Selected icon",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }

                // Color Picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(
                            Color(selectedColor.toInt()),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { showColorPicker = true }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Tap to pick color",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onCreate(name, selectedIcon, selectedColor, selectedType)
                },
                enabled = name.isNotBlank()
            ) {
                Text("Create")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showColorPicker) {
        ColorPickerDialog(
            currentColor = Color(selectedColor.toInt()),
            onColorSelected = { color ->
                selectedColor = color.toArgb().toLong()
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    if (showIconPicker) {
        IconPickerDialog(
            selectedIconId = selectedIcon,
            onIconSelected = { icon ->
                selectedIcon = icon
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false }
        )
    }
}

@Composable
private fun EditCategoryDialog(
    category: CategoryEntity,
    onDismiss: () -> Unit,
    onUpdate: (String, Int, Long) -> Unit
) {
    var name by remember { mutableStateOf(category.name) }
    var selectedIcon by remember { mutableIntStateOf(category.icon) }
    var selectedColor by remember { mutableLongStateOf(category.color) }
    var showColorPicker by remember { mutableStateOf(false) }
    var showIconPicker by remember { mutableStateOf(false) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Edit Category")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name Input
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Icon Picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(
                            Color(selectedColor.toInt()),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { showIconPicker = true }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = if (selectedIcon != 0) selectedIcon else R.drawable.ic_default_category),
                        contentDescription = "Selected icon",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }

                // Color Picker
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .background(
                            Color(selectedColor.toInt()),
                            RoundedCornerShape(8.dp)
                        )
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.outline,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable { showColorPicker = true }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Tap to pick color",
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdate(name, selectedIcon, selectedColor)
                },
                enabled = name.isNotBlank()
            ) {
                Text("Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )

    if (showColorPicker) {
        ColorPickerDialog(
            currentColor = Color(selectedColor.toInt()),
            onColorSelected = { color ->
                selectedColor = color.toArgb().toLong()
                showColorPicker = false
            },
            onDismiss = { showColorPicker = false }
        )
    }

    if (showIconPicker) {
        IconPickerDialog(
            selectedIconId = selectedIcon,
            onIconSelected = { icon ->
                selectedIcon = icon
                showIconPicker = false
            },
            onDismiss = { showIconPicker = false }
        )
    }
}
