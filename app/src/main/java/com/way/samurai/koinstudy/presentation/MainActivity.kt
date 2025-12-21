package com.way.samurai.koinstudy.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.way.samurai.koinstudy.presentation.state.UserUiState
import com.way.samurai.koinstudy.presentation.state.UserViewModel
import org.koin.androidx.compose.getViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    UserScreen()
                }
            }
        }
    }
}

@Composable
fun UserScreen(viewModel: UserViewModel = getViewModel()) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) {
        // initial demo load
        viewModel.load()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Koin Study — Tasks/Profile") })
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            when (state) {
                UserUiState.Idle -> Text("Нажми Load, чтобы получить пользователя")
                UserUiState.Loading -> Text("Loading…")
                is UserUiState.Content -> {
                    val user = (state as UserUiState.Content).user
                    Text(user.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(user.title.value, style = MaterialTheme.typography.titleMedium)
                    Text(user.bio.value, style = MaterialTheme.typography.bodyMedium)
                }
                is UserUiState.Error -> Text(
                    (state as UserUiState.Error).message.ifEmpty { "Unknown error" },
                    color = MaterialTheme.colorScheme.error,
                )
            }

            Button(onClick = { viewModel.load() }) {
                Text("Load / Refresh")
            }
        }
    }
}
