package com.way.samurai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.way.samurai.ui.theme.SamuraiTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SamuraiTheme {
                CoroutineExamplesScreen()
            }
        }
    }
}

@Composable
private fun CoroutineExamplesScreen() {
    val lifecycleOwner = LocalLifecycleOwner.current
    val logs = remember { mutableStateListOf("Нажмите кнопку, чтобы запустить пример корутины.") }
    val repeatingJob = remember { mutableStateOf<Job?>(null) }
    val exceptionHandler = remember {
        CoroutineExceptionHandler { _, throwable ->
            logs.prepend("Исключение поймано в CoroutineExceptionHandler: ${throwable.message}")
        }
    }

    Scaffold(
            topBar = { TopAppBar(title = { Text("Coroutine showcase") }) }
    ) { paddingValues ->
        val lifecycleScope = remember(lifecycleOwner) {
            lifecycleOwner.lifecycleScope
        }

        Column(
                modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                    text = "Практика корутин в одной активности",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
            )
            Text(
                    text = "Каждая кнопка запускает наглядный пример: переключение контекстов, параллельное выполнение, отмена, таймауты, последовательные запросы и сбор Flow."
            )

            Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        lifecycleScope.launch {
                            logs.prepend("withContext: запускаем IO-задачу…")
                            val result = withContext(Dispatchers.IO) { simulateWork("IO-задача", 800) }
                            logs.prepend("withContext: завершено → $result")
                        }
                    }
            ) {
                Text("Пример withContext (IO)")
            }

            Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        lifecycleScope.launch {
                            logs.prepend("async: запускаем два параллельных запроса")
                            val user = async(Dispatchers.Default) { simulateWork("Запрос профиля", 600) }
                            val posts = async(Dispatchers.Default) { simulateWork("Запрос постов", 900) }
                            logs.prepend(
                                    "async: результаты готовы:\n• ${user.await()}\n• ${posts.await()}"
                            )
                        }
                    }
            ) {
                Text("Параллельные async-запросы")
            }

            Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        lifecycleScope.launch {
                            logs.prepend("Последовательно: выполняем шаги один за другим")
                            val first = simulateWork("Первый шаг", 400)
                            val second = simulateWork("Второй шаг", 500)
                            logs.prepend("Последовательно: итоги → $first, затем $second")
                        }
                    }
            ) {
                Text("Последовательные suspend-вызовы")
            }

            Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        lifecycleScope.launch {
                            logs.prepend("Flow: начинаем сбор значений")
                            numberFlow()
                                    .onEach { logs.prepend("Flow: получили $it") }
                                    .collect {
                                        // no-op, логируем выше
                                    }
                            logs.prepend("Flow: сбор завершён")
                        }
                    }
            ) {
                Text("Сбор значений Flow")
            }

            Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        lifecycleScope.launch(exceptionHandler) {
                            logs.prepend("Исключения: бросаем ошибку внутри launch")
                            delay(150)
                            error("Демонстрация ошибки в корутине")
                        }
                    }
            ) {
                Text("Обработка исключений в корутине")
            }

            Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = repeatingJob.value == null,
                    onClick = {
                        repeatingJob.value = lifecycleScope.launch {
                            logs.prepend("Повторы: стартуем отслеживание каждые 300 мс")
                            repeat(6) { index ->
                                logs.prepend("Повторы: шаг $index на ${Thread.currentThread().name}")
                                delay(300)
                            }
                            logs.prepend("Повторы: завершились автоматически")
                            repeatingJob.value = null
                        }
                    }
            ) {
                Text("Запустить повторяющуюся задачу")
            }

            Button(
                    modifier = Modifier.fillMaxWidth(),
                    enabled = repeatingJob.value != null,
                    onClick = {
                        repeatingJob.value?.cancel()
                        logs.prepend("Повторы: остановлено вручную")
                        repeatingJob.value = null
                    }
            ) {
                Text("Остановить повторяющуюся задачу")
            }

            Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        lifecycleScope.launch {
                            logs.prepend("withTimeout: пытаемся уложиться в 700 мс")
                            try {
                                withTimeout(700) {
                                    repeat(5) { step ->
                                        delay(200)
                                        logs.prepend("withTimeout: прогресс $step")
                                    }
                                }
                                logs.prepend("withTimeout: успели завершить задачу")
                            } catch (timeout: TimeoutCancellationException) {
                                logs.prepend("withTimeout: таймаут — задача отменена")
                            }
                        }
                    }
            ) {
                Text("Пример с таймаутом и отменой")
            }

            Card(
                    modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
            ) {
                LazyColumn(
                        modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(logs) { entry ->
                        Text(entry)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                    text = "Поток: ${Thread.currentThread().name}. Новые сообщения появляются сверху.",
                    style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun MutableList<String>.prepend(entry: String) {
    add(0, entry)
    if (size > 40) {
        removeLast()
    }
}

private suspend fun simulateWork(name: String, durationMs: Long): String {
    delay(durationMs)
    return "$name завершена на потоке ${Thread.currentThread().name}"
}

private fun numberFlow(): Flow<Int> = flow {
    for (value in 1..6) {
        delay(150)
        emit(value)
    }
}.flowOn(Dispatchers.Default)
