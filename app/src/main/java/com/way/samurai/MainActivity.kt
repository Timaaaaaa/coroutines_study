package com.way.samurai

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.lifecycleScope
import com.way.samurai.ui.theme.SamuraiTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SamuraiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                            name = "Android",
                            modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
        clearify()
    }
}

private fun launchExample(lifecycleScope: LifecycleCoroutineScope) {
    lifecycleScope.launch {
        while (true) {
            delay(1000)
            Log.d("still running", "on lifecycleScope")
        }
    }
}

private fun runBlockingExample() = runBlocking {
    Log.d("current suspend runBlocking", "onCreate: ${Thread.currentThread().name}")
    delay(2000)
    Log.d("current suspend runBlocking", "onCreate: ${Thread.currentThread().name}")
}

private fun withContextExample(context: Context) {
    GlobalScope.launch {
        withTimeout(3000) {
            for (i in 1..5) {
                if (isActive) {
                    Log.d("current thread fib", "fib of " + i + " = " + fib(i))
                }
            }
        }

        val response = getResponse()
        println(response)
        withContext(Dispatchers.Main) {
            Toast.makeText(context, response, Toast.LENGTH_SHORT).show()
        }
        Log.d("current thread name", "onCreate: ${Thread.currentThread().name}")
    }
}

private fun asyncExample() {
    GlobalScope.launch {
        val call = launch { call1() }
        val call1 = async { call1() }
        val call2 = async { call2() }
        Log.d("current thread name", "onCreate: ${Thread.currentThread().name}")
        Log.d("current thread name", "onCreate: ${call1.await()} ${call2.await()}")
        Log.d("current thread name", "onCreate: ${call}")
    }
    Log.d("current thread name", "onCreate: ${Thread.currentThread().name}")
}

private fun clearify() = runBlocking {
    Log.d("clearify", "start")
    val call1 = launch {
        Log.d("clearify-launch", "1")
        callWithLogs("clearify-launch")
        Log.d("clearify-launch", "2")

    }
    val call2 = withContext(Dispatchers.IO) {
        Log.d("clearify-withContext", "1")
        callWithLogs("clearify-withContext")
        Log.d("clearify-withContext", "2")
    }
    Log.d("clearify", "end - $call1 - $call2")
}

suspend fun callWithLogs(source: String): String {
    Log.d( source + "-callWithLogs", "1")
    delay(3000)
    Log.d( source + "-callWithLogs", "2")
    return "call1"
}

suspend fun call1(): String {
    delay(3000)
    return "call1"
}

suspend fun call2(): String {

    delay(5000)
    return "call2"

}

private fun fib(n: Int): Int {
    return if (n < 2) {
        n
    } else {
        fib(n - 1) + fib(n - 2)
    }

}

private suspend fun getResponse(): String {
    delay(3000)
    return "Answer"
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
    SamuraiTheme {
        Greeting("Android")
    }
}