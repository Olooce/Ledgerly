package ke.ac.ku.ledgerly.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
                val path = this

                path.lineTo(0f, size.height)
                path.quadraticBezierTo(
                    size.width / 2f,
                    size.height - 100f,
                    size.width,
                    size.height
                )
                path.lineTo(size.width, 0f)
                path.close()
            }
        )
    }
}

@Composable
fun LedgerlyTopBar(
    modifier: Modifier = Modifier,
    height: Dp = 140.dp,
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
            ),
        content = content
    )
}