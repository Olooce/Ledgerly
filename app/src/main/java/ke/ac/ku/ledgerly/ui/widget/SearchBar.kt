package ke.ac.ku.ledgerly.ui.widget

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import ke.ac.ku.ledgerly.R

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    var text by remember { mutableStateOf(searchQuery) }

    val scheme = MaterialTheme.colorScheme
    val borderColor = scheme.outlineVariant
    val iconColor = scheme.onSurfaceVariant
    val placeholderColor = scheme.onSurfaceVariant.copy(alpha = 0.7f)
    val textColor = scheme.onSurface
    val backgroundColor = scheme.surfaceContainerHighest

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor, RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_search),
                contentDescription = "Search",
                colorFilter = ColorFilter.tint(iconColor),
                modifier = Modifier
                    .size(20.dp)
                    .padding(end = 8.dp)
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 2.dp)
            ) {
                if (text.isEmpty()) {
                    TransactionTextView(
                        text = "Search transactions…",
                        style = MaterialTheme.typography.bodyMedium,
                        color = placeholderColor
                    )
                }

                BasicTextField(
                    value = text,
                    onValueChange = {
                        text = it
                        onSearchQueryChange(it)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = MaterialTheme.typography.bodyMedium.copy(color = textColor),
                    cursorBrush = SolidColor(scheme.primary)
                )
            }

            if (text.isNotEmpty()) {
                Image(
                    painter = painterResource(id = R.drawable.ic_clear),
                    contentDescription = "Clear",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                            text = ""
                            onSearchQueryChange("")
                        }
                        .padding(start = 8.dp),
                    colorFilter = ColorFilter.tint(iconColor)
                )
            }

            Image(
                painter = painterResource(id = R.drawable.ic_close),
                contentDescription = "Close search",
                modifier = Modifier
                    .size(20.dp)
                    .clickable { onCloseSearch() }
                    .padding(start = 8.dp),
                colorFilter = ColorFilter.tint(iconColor)
            )
        }
    }
}
