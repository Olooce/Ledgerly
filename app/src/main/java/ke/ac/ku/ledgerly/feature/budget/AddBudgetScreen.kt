@file:OptIn(ExperimentalMaterial3Api::class)

package ke.ac.ku.ledgerly.feature.budget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import ke.ac.ku.ledgerly.ui.theme.InterFontFamily
import ke.ac.ku.ledgerly.ui.theme.LightGrey
import ke.ac.ku.ledgerly.ui.theme.Typography
import ke.ac.ku.ledgerly.utils.Utils
import ke.ac.ku.ledgerly.widget.TransactionTextView

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
                }
            )
        }
    }
}

@Composable
fun AddBudgetForm(
    modifier: Modifier,
    onAddBudget: (BudgetEntity) -> Unit
) {
    val category = remember { mutableStateOf("") }
    val monthlyBudget = remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth()
            .shadow(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        TitleComponent("Category")
        TransactionDropDown(
            listOf(
                "Grocery", "Rent", "Utilities", "Transport", "Education",
                "Entertainment", "Healthcare", "Insurance", "Dining Out",
                "Travel", "Shopping", "Subscriptions", "Other"
            )
        ) { selected -> category.value = selected }

        Spacer(modifier = Modifier.size(24.dp))

        TitleComponent("Monthly Budget Amount")
        OutlinedTextField(
            value = monthlyBudget.value,
            onValueChange = { monthlyBudget.value = it.filter { ch -> ch.isDigit() || ch == '.' } },
            textStyle = TextStyle(color = Color.Black),
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { TransactionTextView(text = "Enter amount") },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
            )
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
            enabled = category.value.isNotEmpty() && monthlyBudget.value.isNotEmpty()
        ) {
            TransactionTextView(
                text = "Add Budget",
                fontSize = 14.sp,
                color = Color.White
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

@Composable
fun TransactionDropDown(listOfItems: List<String>, onItemSelected: (item: String) -> Unit) {
    val expanded = remember { mutableStateOf(false) }
    val selectedItem = remember { mutableStateOf(listOfItems[0]) }

    ExposedDropdownMenuBox(expanded = expanded.value, onExpandedChange = { expanded.value = it }) {
        OutlinedTextField(
            value = selectedItem.value,
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            textStyle = TextStyle(fontFamily = InterFontFamily, color = Color.Black),
            readOnly = true,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value) },
            shape = RoundedCornerShape(8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Black,
                unfocusedBorderColor = Color.Black,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black
            )
        )

        ExposedDropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }) {
            listOfItems.forEach {
                DropdownMenuItem(text = { TransactionTextView(text = it) }, onClick = {
                    selectedItem.value = it
                    onItemSelected(it)
                    expanded.value = false
                })
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAddBudgetScreen() {
    AddBudgetScreen(rememberNavController())
}
