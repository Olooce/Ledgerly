package ke.ac.ku.ledgerly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp


class CurvedTopAppBarShape : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        return Outline.Generic(
            path = Path().apply {
                reset()
                moveTo(0f, 0f)
                lineTo(0f, size.height)
                // Creates an S-curve (like an integral/differentiation symbol)
                // Lower on the left (size.height), higher on the right (size.height * 0.6f)
                cubicTo(
                    x1 = size.width * 0.4f, y1 = size.height,
                    x2 = size.width * 0.6f, y2 = size.height * 0.5f,
                    x3 = size.width, y3 = size.height * 0.6f
                )
                lineTo(size.width, 0f)
                close()
            }
        )
    }
}

@Composable
fun LedgerlyTopBar(
    modifier: Modifier = Modifier,
    height: Dp = 200.dp,
    colors: List<Color> = listOf(
        Color(0xFF009650),
        Color(0xFF009670)
    ),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(
                brush = Brush.linearGradient(
                    colors = colors
                ),
                shape = CurvedTopAppBarShape()
            )
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            content = content
        )
    }
}
