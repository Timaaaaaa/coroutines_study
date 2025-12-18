package com.way.samurai

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.way.samurai.ui.theme.SamuraiTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

class FirstActivity() : ComponentActivity(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() {
            return Dispatchers.IO
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SamuraiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                            name = "Android FirstActivity",
                            modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        GlobalScope.launch {
            Log.d("current thread name", "onCreate: ${Thread.currentThread().name}")
        }
        Log.d("current thread name", "onCreate: ${Thread.currentThread().name}")
    }
}