import org.gradle.api.tasks.JavaExec

plugins {
    alias(libs.plugins.kotlin.jvm)
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.debug)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}

val lessonMains = mapOf(
    "Lesson01Basics" to "com.way.samurai.lessons.Lesson01BasicsKt",
    "Lesson02Context" to "com.way.samurai.lessons.Lesson02ContextAndDispatchersKt",
    "Lesson03Cancellation" to "com.way.samurai.lessons.Lesson03CancellationAndTimeoutsKt",
    "Lesson04Structured" to "com.way.samurai.lessons.Lesson04StructuredConcurrencyKt",
    "Lesson05Exceptions" to "com.way.samurai.lessons.Lesson05ExceptionsAndSupervisionKt",
    "Lesson06FlowVsChannel" to "com.way.samurai.lessons.Lesson06FlowVsChannelKt",
    "Lesson07ConcurrencyPrimitives" to "com.way.samurai.lessons.Lesson07ConcurrencyPrimitivesKt",
    "Lesson08Performance" to "com.way.samurai.lessons.Lesson08PerformanceKt",
    "Lesson09Testing" to "com.way.samurai.lessons.Lesson09TestingKt",
    "Lesson10AndroidPatterns" to "com.way.samurai.lessons.Lesson10AndroidPatternsKt",
)

lessonMains.forEach { (taskName, mainClassName) ->
    tasks.register<JavaExec>("run$taskName") {
        group = "application"
        description = "Runs $taskName examples"
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set(mainClassName)
    }
}
