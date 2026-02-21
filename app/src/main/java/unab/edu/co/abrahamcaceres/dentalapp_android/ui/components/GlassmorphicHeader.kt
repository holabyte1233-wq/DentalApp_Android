package unab.edu.co.abrahamcaceres.dentalapp_android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.AccentBlue
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.BorderLight

@Composable
fun GlassmorphicHeader(
    title: String,
    modifier: Modifier = Modifier,
    onLogout: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(Color.White.copy(alpha = 0.7f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (onLogout != null) {
                TextButton(
                    onClick = onLogout,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "Salir",
                        color = AccentBlue,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(BorderLight)
        )
    }
}
