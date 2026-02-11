package unab.edu.co.abrahamcaceres.dentalapp_android.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Calendar
import java.util.Locale

@Composable
fun ClockDisplay(
    modifier: Modifier = Modifier,
    fontSize: androidx.compose.ui.unit.TextUnit = 56.sp,
    lightWeight: Boolean = true
) {
    var time by remember { mutableStateOf(formatTime(Calendar.getInstance())) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            time = formatTime(Calendar.getInstance())
        }
    }
    Text(
        text = time,
        modifier = modifier,
        style = MaterialTheme.typography.displayLarge.copy(
            fontSize = fontSize,
            fontWeight = if (lightWeight) FontWeight.Light else FontWeight.Normal
        ),
        color = MaterialTheme.colorScheme.onSurface
    )
}

private fun formatTime(cal: Calendar): String {
    return String.format(Locale("es", "ES"), "%02d:%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
}
