package ke.ac.ku.ledgerly.utils

import android.content.Context
import android.graphics.drawable.Drawable
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

    fun drawableToPainter(context: Context, @DrawableRes resId: Int): Painter? {
        return try {
            val drawable = AppCompatResources.getDrawable(context, resId)
            drawable?.let {
                val bitmap = it.toBitmap(
                    it.intrinsicWidth,
                    it.intrinsicHeight,
                    android.graphics.Bitmap.Config.ARGB_8888
                )
                BitmapPainter(bitmap.asImageBitmap())
            }
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