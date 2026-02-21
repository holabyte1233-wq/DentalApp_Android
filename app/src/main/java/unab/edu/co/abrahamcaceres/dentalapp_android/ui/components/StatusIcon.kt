package unab.edu.co.abrahamcaceres.dentalapp_android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.sqrt

/**
 * Icon with optional "forbidden" overlay when unavailable.
 *
 * @param icon The base icon (e.g. sun, face)
 * @param isAvailable When false, overlays a red circle with diagonal line (forbidden symbol)
 * @param modifier Modifier for the container
 * @param iconSize Size of the base icon
 * @param contentDescription Content description for accessibility
 * @param tint Tint color for the icon when available
 */
@Composable
fun StatusIcon(
    icon: ImageVector,
    isAvailable: Boolean,
    modifier: Modifier = Modifier,
    iconSize: Dp = 22.dp,
    contentDescription: String? = null,
    tint: Color = Color.Black
) {
    val forbiddenRed = Color(0xFFFF3B30)
    val strokeWidthDp = 2.dp

    Box(
        modifier = modifier.size(iconSize + 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier
                .size(iconSize)
                .graphicsLayer {
                    alpha = if (isAvailable) 1f else 0.5f
                },
            tint = tint
        )
        if (!isAvailable) {
            Canvas(modifier = Modifier.size(iconSize + 8.dp)) {
                val strokeWidth = strokeWidthDp.toPx()
                val radius = (size.minDimension - strokeWidth) / 2f
                val cx = size.width / 2f
                val cy = size.height / 2f

                // Red circle outline (forbidden ring) - 2dp stroke
                drawCircle(
                    color = forbiddenRed,
                    radius = radius,
                    center = Offset(cx, cy),
                    style = Stroke(width = strokeWidth)
                )

                // Diagonal line at 45 degrees (crossing the icon)
                val halfLen = radius * 1.2f
                val cos45 = sqrt(2f) / 2f
                val sin45 = cos45
                val x1 = cx - halfLen * cos45
                val y1 = cy - halfLen * sin45
                val x2 = cx + halfLen * cos45
                val y2 = cy + halfLen * sin45

                drawLine(
                    color = forbiddenRed,
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}
