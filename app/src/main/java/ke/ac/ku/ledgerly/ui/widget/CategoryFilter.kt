package ke.ac.ku.ledgerly.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ke.ac.ku.ledgerly.R
import ke.ac.ku.ledgerly.data.model.CategoryEntity
import ke.ac.ku.ledgerly.ui.theme.Typography

@Composable
fun CategoryFilter(
    availableCategories: List<CategoryEntity>,
    selectedCategories: List<String>,
    onCategoriesChange: (List<String>) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val surfaceColor = MaterialTheme.colorScheme.surface
    val primaryTextColor = MaterialTheme.colorScheme.onSurface
    val secondaryTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = MaterialTheme.colorScheme.outline
    val primaryColor = MaterialTheme.colorScheme.primary

    Column(modifier = modifier) {
        TransactionTextView(
            text = "Categories",
            style = Typography.bodySmall,
            color = primaryTextColor,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(surfaceColor, RoundedCornerShape(8.dp))
                .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                .clickable { expanded = true }
                .padding(12.dp)
        ) {
            TransactionTextView(
                text = if (selectedCategories.isEmpty()) "All Categories"
                else "${selectedCategories.size} selected",
                style = Typography.bodyMedium,
                color = if (selectedCategories.isEmpty()) secondaryTextColor else primaryTextColor
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(surfaceColor)
        ) {
            availableCategories.forEach { category ->
                DropdownMenuItem(
                    text = {
                        TransactionTextView(
                            text = category.name,
                            style = Typography.bodyMedium,
                            color = primaryTextColor
                        )
                    },
                    onClick = {
                        val newSelection = if (selectedCategories.contains(category.id)) {
                            selectedCategories - category.id
                        } else {
                            selectedCategories + category.id
                        }
                        onCategoriesChange(newSelection)
                    },
                    trailingIcon = {
                        if (selectedCategories.contains(category.id)) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_check),
                                contentDescription = "Selected",
                                colorFilter = ColorFilter.tint(primaryColor),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                )
            }
        }
    }
}