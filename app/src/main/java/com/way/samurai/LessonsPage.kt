package com.way.samurai

import com.way.samurai.lessons.Lesson01BasicsLauncher
import com.way.samurai.lessons.Lesson02ContextLauncher
import com.way.samurai.lessons.Lesson03CancellationLauncher
import com.way.samurai.lessons.Lesson04StructuredLauncher
import com.way.samurai.lessons.Lesson05ExceptionsLauncher
import com.way.samurai.lessons.Lesson06FlowVsChannelLauncher
import com.way.samurai.lessons.Lesson07ConcurrencyPrimitivesLauncher
import com.way.samurai.lessons.Lesson08PerformanceLauncher
import com.way.samurai.lessons.Lesson09TestingLauncher
import com.way.samurai.lessons.Lesson10AndroidPatternsLauncher

object LessonsPage {
    @JvmStatic
    fun main(args: Array<String>) {
        println("=== Coroutines lessons page ===")
        println("Launching all demos in sequence...")
        listOf(
            Lesson01BasicsLauncher::main,
            Lesson02ContextLauncher::main,
            Lesson03CancellationLauncher::main,
            Lesson04StructuredLauncher::main,
            Lesson05ExceptionsLauncher::main,
            Lesson06FlowVsChannelLauncher::main,
            Lesson07ConcurrencyPrimitivesLauncher::main,
            Lesson08PerformanceLauncher::main,
            Lesson09TestingLauncher::main,
            Lesson10AndroidPatternsLauncher::main,
        ).forEach { launcher ->
            try {
                launcher(emptyArray())
            } catch (t: Throwable) {
                // CancellationException is normal; other errors are logged for visibility.
                println("Launcher ${launcher::class.simpleName} finished with: ${t.message}")
            }
            println("----")
        }
        println("All demos finished.")
    }
}
