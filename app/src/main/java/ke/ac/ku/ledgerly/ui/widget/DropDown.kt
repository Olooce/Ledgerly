package ke.ac.ku.ledgerly.ui.widget

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import ke.ac.ku.ledgerly.ui.theme.InterFontFamily


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropDown(listOfItems: List<String>, onItemSelected: (item: String) -> Unit) {
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


