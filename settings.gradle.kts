pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
    plugins {
        id("com.android.application") version "8.11.1"
        id("org.jetbrains.kotlin.android") version "2.0.21"
        id("org.jetbrains.kotlin.jvm") version "2.0.21"
        id("org.jetbrains.kotlin.plugin.compose") version "2.0.21"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        google()
    }
}

rootProject.name = "coroutines-study"

include(":core")
include(":app")
include(":examples:01_basics")
include(":examples:02_context_and_dispatchers")
include(":examples:03_cancellation_and_timeouts")
include(":examples:04_structured_concurrency")
include(":examples:05_exceptions_supervision")
include(":examples:06_flow_vs_channel")
include(":examples:07_concurrency_primitives")
include(":examples:08_performance")
include(":examples:09_testing")
include(":examples:10_android_patterns")
 
