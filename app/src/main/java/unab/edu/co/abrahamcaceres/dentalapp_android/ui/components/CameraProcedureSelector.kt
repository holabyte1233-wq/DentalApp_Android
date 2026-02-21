package unab.edu.co.abrahamcaceres.dentalapp_android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

data class ProcedureTab(
    val icon: ImageVector,
    val label: String,
    val showNotification: Boolean = false,
    val enabled: Boolean = true
)

@Composable
fun CameraProcedureSelector(
    procedures: List<ProcedureTab>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val glassmorphismBg = Color.Black.copy(alpha = 0.4f)
    val selectedPillColor = Color.White
    val unselectedIconTint = Color.White.copy(alpha = 0.9f)
    val density = LocalDensity.current

    val tabBounds = remember { mutableStateListOf<Pair<Float, Float>>() }
    val containerLeft = remember { mutableStateOf(0f) } // root x of container, for tab position calc

    val pillTargetLeft = if (tabBounds.size > selectedIndex && selectedIndex >= 0) {
        tabBounds.getOrNull(selectedIndex)?.first ?: 0f
    } else 0f
    val pillTargetWidth = if (tabBounds.size > selectedIndex && selectedIndex >= 0) {
        tabBounds.getOrNull(selectedIndex)?.second ?: 80f
    } else 80f

    val animatedPillLeft by animateFloatAsState(
        targetValue = pillTargetLeft,
        animationSpec = tween(durationMillis = 300),
        label = "pill_slide_left"
    )
    val animatedPillWidth by animateFloatAsState(
        targetValue = pillTargetWidth,
        animationSpec = tween(durationMillis = 300),
        label = "pill_slide_width"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(glassmorphismBg)
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .onGloballyPositioned { coordinates ->
                containerLeft.value = coordinates.positionInRoot().x
            }
    ) {
        // 1. Sliding white pill (drawn first = behind)
        if (tabBounds.isNotEmpty() && selectedIndex in tabBounds.indices) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .offset(x = with(density) { animatedPillLeft.toDp() })
                    .padding(horizontal = 2.dp)
                    .size(
                        width = with(density) { animatedPillWidth.toDp() },
                        height = 36.dp
                    )
                    .clip(RoundedCornerShape(18.dp))
                    .background(selectedPillColor.copy(alpha = 0.95f))
            )
        }

        // 2. Tab content (drawn on top)
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            procedures.forEachIndexed { index, tab ->
                val isSelected = index == selectedIndex
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable(enabled = tab.enabled) { onTabSelected(index) }
                        .padding(
                            horizontal = if (isSelected) 14.dp else 10.dp,
                            vertical = 10.dp
                        )
                        .onGloballyPositioned { coordinates ->
                            val rootPos = coordinates.positionInRoot()
                            val leftInContainer = rootPos.x - containerLeft.value
                            val w = coordinates.size.width.toFloat()
                            if (tabBounds.size <= index) {
                                while (tabBounds.size <= index) tabBounds.add(0f to 0f)
                            }
                            tabBounds[index] = leftInContainer to w
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatusIcon(
                            icon = tab.icon,
                            isAvailable = tab.enabled,
                            modifier = Modifier.size(26.dp),
                            iconSize = 18.dp,
                            contentDescription = tab.label,
                            tint = if (isSelected) Color.Black else unselectedIconTint
                        )
                        AnimatedVisibility(
                            visible = isSelected,
                            enter = expandHorizontally(
                                expandFrom = Alignment.Start,
                                animationSpec = tween(200)
                            ),
                            exit = shrinkHorizontally(
                                shrinkTowards = Alignment.Start,
                                animationSpec = tween(200)
                            )
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Spacer(modifier = Modifier.size(6.dp))
                                Text(
                                    text = tab.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
