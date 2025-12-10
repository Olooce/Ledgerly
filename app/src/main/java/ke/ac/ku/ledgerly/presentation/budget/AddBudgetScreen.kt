@file:OptIn(ExperimentalMaterial3Api::class)

package ke.ac.ku.ledgerly.presentation.budget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import ke.ac.ku.ledgerly.R
import ke.ac.ku.ledgerly.data.model.BudgetEntity
import ke.ac.ku.ledgerly.data.repository.CategoryRepository
import ke.ac.ku.ledgerly.ui.theme.LightGrey
import ke.ac.ku.ledgerly.ui.theme.Typography
import ke.ac.ku.ledgerly.ui.widget.TransactionTextView
import ke.ac.ku.ledgerly.utils.Utils

@Composable
fun AddBudgetScreen(
    navController: NavController,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val menuExpanded = remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize()) {
        ConstraintLayout(modifier = Modifier.fillMaxSize()) {
            val (nameRow, formCard, topBar) = createRefs()

            Image(
                painter = painterResource(id = R.drawable.ic_topbar),
                contentDescription = null,
                modifier = Modifier.constrainAs(topBar) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
            )

            // Top Row
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 64.dp, start = 16.dp, end = 16.dp)
                    .constrainAs(nameRow) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                    }
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_back),
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .clickable { navController.popBackStack() }
                )

                TransactionTextView(
                    text = "Add Budget",
                    style = Typography.titleLarge,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.Center)
                )

                Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                    Image(
                        painter = painterResource(id = R.drawable.dots_menu),
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .clickable { menuExpanded.value = true }
                    )
                    DropdownMenu(
                        expanded = menuExpanded.value,
                        onDismissRequest = { menuExpanded.value = false }
                    ) {
                        DropdownMenuItem(
                            text = { TransactionTextView(text = "Profile") },
                            onClick = { menuExpanded.value = false }
                        )
                        DropdownMenuItem(
                            text = { TransactionTextView(text = "Settings") },
                            onClick = { menuExpanded.value = false }
                        )
                    }
                }
            }

            AddBudgetForm(
                modifier = Modifier.constrainAs(formCard) {
                    top.linkTo(nameRow.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
                onAddBudget = {
                    viewModel.setBudget(it)
                    navController.popBackStack()
                },
                categoryRepository = viewModel.categoryRepository
            )
        }
    }
}

@Composable
fun AddBudgetForm(
    modifier: Modifier,
    onAddBudget: (BudgetEntity) -> Unit,
    categoryRepository: CategoryRepository
) {
    val category = remember { mutableStateOf("") }
    val monthlyBudget = remember { mutableStateOf("") }

    // Load expense categories
    val categories by categoryRepository.getCategoriesByTypeFlow("Expense")
        .collectAsState(initial = emptyList())
    val sortedCategories = remember(categories) {
        categories.sortedBy { it.name }
    }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .shadow(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TitleComponent("Category")

        var expandedCategory by remember { mutableStateOf(false) }
        val selectedCategoryName = remember(category.value, sortedCategories) {
            sortedCategories.find { it.id == category.value }?.name ?: ""
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expandedCategory = true }
                .background(MaterialTheme.colorScheme.surface)
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))
                .padding(12.dp)
        ) {
            TransactionTextView(
                text = if (selectedCategoryName.isEmpty()) "Select category" else selectedCategoryName,
                color = if (selectedCategoryName.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface
            )
        }

        DropdownMenu(
            expanded = expandedCategory,
            onDismissRequest = { expandedCategory = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            if (sortedCategories.isEmpty()) {
                DropdownMenuItem(
                    text = { TransactionTextView(text = "No categories available") },
                    onClick = { expandedCategory = false },
                    enabled = false
                )
            } else {
                sortedCategories.forEach { categoryEntity ->
                    DropdownMenuItem(
                        text = { TransactionTextView(text = categoryEntity.name) },
                        onClick = {
                            category.value = categoryEntity.id
                            expandedCategory = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.size(24.dp))

        TitleComponent("Monthly Budget Amount")
        OutlinedTextField(
            value = monthlyBudget.value,
            onValueChange = { monthlyBudget.value = it.filter { ch -> ch.isDigit() || ch == '.' } },
            textStyle = TextStyle(color = MaterialTheme.colorScheme.onSurface),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { TransactionTextView(text = "Enter amount") }
        )

        Spacer(modifier = Modifier.size(32.dp))

        Button(
            onClick = {
                val model = BudgetEntity(
                    category = category.value,
                    monthlyBudget = monthlyBudget.value.toDoubleOrNull() ?: 0.0,
                    currentSpending = 0.0,
                    monthYear = Utils.getCurrentMonthYear()
                )
                onAddBudget(model)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            enabled = category.value.isNotEmpty() && monthlyBudget.value.isNotEmpty(),
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            TransactionTextView(
                text = "Add Budget",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}


@Composable
fun TitleComponent(title: String) {
    TransactionTextView(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = LightGrey
    )
    Spacer(modifier = Modifier.size(10.dp))
}

@Preview(showBackground = true)
@Composable
fun PreviewAddBudgetScreen() {
    AddBudgetScreen(rememberNavController())
}
