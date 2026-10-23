package it.nicolatomassoni.gandy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import it.nicolatomassoni.gandy.ui.navigation.GandyNavHost
import it.nicolatomassoni.gandy.ui.theme.GandyTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GandyTheme {
                GandyNavHost()
            }
        }
    }
}
