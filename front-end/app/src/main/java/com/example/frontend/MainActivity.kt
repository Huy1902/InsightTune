package com.example.frontend

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.content.MediaType.Companion.Text
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.ContentDataType.Companion.Text
import androidx.compose.ui.semantics.SemanticsProperties.Text
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.exoplayer.ExoPlayer
import com.example.frontend.ui.playingsong.MusicPlayer
import com.example.frontend.ui.playingsong.MusicPlayerViewModel
import com.example.frontend.ui.theme.FrontEndTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val exoPlayer = ExoPlayer.Builder(this).build()

        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return MusicPlayerViewModel(exoPlayer) as T
            }
        }
        enableEdgeToEdge()
        setContent {
            FrontEndTheme {
//                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
//                    Greeting(
//                        name = "Android",
//                        modifier = Modifier.padding(innerPadding)
//                    )
//                }
                val musicPlayerViewModel: MusicPlayerViewModel = viewModel(factory = viewModelFactory)
                MusicPlayer(viewModel = musicPlayerViewModel)
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FrontEndTheme {
        Greeting("Android")
    }
}