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
    implementation(libs.kotlinx.coroutines.test)

    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
}

val lessonMains = mapOf(
    "LessonsPage" to "com.way.samurai.LessonsPage",
    "Lesson01Basics" to "com.way.samurai.lessons.Lesson01BasicsLauncher",
    "Lesson02Context" to "com.way.samurai.lessons.Lesson02ContextLauncher",
    "Lesson03Cancellation" to "com.way.samurai.lessons.Lesson03CancellationLauncher",
    "Lesson04Structured" to "com.way.samurai.lessons.Lesson04StructuredLauncher",
    "Lesson05Exceptions" to "com.way.samurai.lessons.Lesson05ExceptionsLauncher",
    "Lesson06FlowVsChannel" to "com.way.samurai.lessons.Lesson06FlowVsChannelLauncher",
    "Lesson07ConcurrencyPrimitives" to "com.way.samurai.lessons.Lesson07ConcurrencyPrimitivesLauncher",
    "Lesson08Performance" to "com.way.samurai.lessons.Lesson08PerformanceLauncher",
    "Lesson09Testing" to "com.way.samurai.lessons.Lesson09TestingLauncher",
    "Lesson10AndroidPatterns" to "com.way.samurai.lessons.Lesson10AndroidPatternsLauncher",
)

lessonMains.forEach { (taskName, mainClassName) ->
    tasks.register<JavaExec>("run$taskName") {
        group = "application"
        description = "Runs $taskName examples"
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set(mainClassName)
    }
}
