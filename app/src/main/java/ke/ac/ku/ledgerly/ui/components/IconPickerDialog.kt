package ke.ac.ku.ledgerly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ke.ac.ku.ledgerly.R

data class CategoryIcon(
    val resourceId: Int,
    val name: String
)

val availableCategoryIcons = listOf(
    CategoryIcon(R.drawable.ic_grocery, "Grocery"),
    CategoryIcon(R.drawable.ic_netflix, "Netflix"),
    CategoryIcon(R.drawable.ic_rent, "Rent"),
    CategoryIcon(R.drawable.ic_paypal, "Paypal"),
    CategoryIcon(R.drawable.ic_starbucks, "Starbucks"),
    CategoryIcon(R.drawable.ic_transport, "Transport"),
    CategoryIcon(R.drawable.ic_utility, "Utilities"),
    CategoryIcon(R.drawable.ic_education, "Education"),
    CategoryIcon(R.drawable.ic_ent, "Entertainment"),
    CategoryIcon(R.drawable.ic_healthcare, "Healthcare"),
    CategoryIcon(R.drawable.ic_investment, "Investments"),
    CategoryIcon(R.drawable.ic_upwork, "Freelance"),
    CategoryIcon(R.drawable.ic_budget, "Budget"),
    CategoryIcon(R.drawable.ic_receipt, "Receipt"),
    CategoryIcon(R.drawable.ic_default_category, "Default"),
)

@Composable
fun IconPickerDialog(
    selectedIconId: Int,
    onIconSelected: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Select Category Icon")
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableCategoryIcons) { icon ->
                        IconOption(
                            icon = icon,
                            isSelected = icon.resourceId == selectedIconId,
                            onClick = { onIconSelected(icon.resourceId) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Done")
            }
        }
    )
}

@Composable
private fun IconOption(
    icon: CategoryIcon,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val painter = painterResource(id = icon.resourceId)

    Box(
        modifier = Modifier
            .size(60.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                else
                    MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painter,
            contentDescription = icon.name,
            modifier = Modifier.size(36.dp),
            tint = if (isSelected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurface
        )
    }
}
