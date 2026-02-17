package unab.edu.co.abrahamcaceres.dentalapp_android

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.handleDeeplinks
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.navigation.AppNavigation
import unab.edu.co.abrahamcaceres.dentalapp_android.ui.theme.DentalApp_AndroidTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var supabaseClient: SupabaseClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        supabaseClient.handleDeeplinks(intent)
        setContent {
            DentalApp_AndroidTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        supabaseClient.handleDeeplinks(intent)
    }
}
