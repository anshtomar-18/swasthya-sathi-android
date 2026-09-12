package com.swasthyasathi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.swasthyasathi.app.ui.MainNavigation
import com.swasthyasathi.app.ui.theme.AppSurface
import com.swasthyasathi.app.ui.theme.SwasthyaSathiTheme
import com.swasthyasathi.app.viewmodel.HealthViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: HealthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            SwasthyaSathiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = AppSurface
                ) {
                    MainNavigation(viewModel = viewModel)
                }
            }
        }
    }
}
