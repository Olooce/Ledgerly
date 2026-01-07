package ke.ac.ku.ledgerly.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.core.graphics.drawable.toBitmap
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat

object DrawableUtils {

    fun getDrawable(context: Context, @DrawableRes resId: Int): Drawable? {
        return try {
            AppCompatResources.getDrawable(context, resId)
        } catch (e: Exception) {
            null
        }
    }

    fun isVectorDrawable(context: Context, @DrawableRes resId: Int): Boolean {
        return try {
            val drawable = AppCompatResources.getDrawable(context, resId)
            drawable is VectorDrawableCompat
        } catch (e: Exception) {
            false
        }
    }

    fun drawableToPainter(
        context: Context,
        @DrawableRes resId: Int,
        fallbackSizeDp: Int = 24
    ): Painter? {
        return try {
            val drawable = AppCompatResources.getDrawable(context, resId) ?: return null

            val density = context.resources.displayMetrics.density

            val width = drawable.intrinsicWidth.takeIf { it > 0 }
                ?: (fallbackSizeDp * density).toInt()

            val height = drawable.intrinsicHeight.takeIf { it > 0 }
                ?: (fallbackSizeDp * density).toInt()

            if (drawable.intrinsicWidth <= 0 || drawable.intrinsicHeight <= 0) {
                Log.w(
                    "DrawableToPainter",
                    "Drawable has no intrinsic size: resId=$resId, " +
                            "width=${drawable.intrinsicWidth}, height=${drawable.intrinsicHeight}"
                )
            }

            val bitmap = drawable.toBitmap(
                width = width,
                height = height,
                config = Bitmap.Config.ARGB_8888
            )

            BitmapPainter(bitmap.asImageBitmap())
        } catch (e: Exception) {
            null
        }
    }

}

@Composable
fun rememberDrawablePainter(@DrawableRes resId: Int): Painter? {
    val context = LocalContext.current
    return remember(resId) {
        DrawableUtils.drawableToPainter(context, resId)
    }
}