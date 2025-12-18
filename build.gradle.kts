// Root build configuration for the coroutine study project.
plugins {
    id("org.jetbrains.kotlin.jvm") apply false
}

subprojects {
    if (name != "app") {
        apply(plugin = "org.jetbrains.kotlin.jvm")
    }

    group = "com.way.coroutines"
    version = "1.0.0"

    // Align Java/Kotlin targets for JVM modules.
    if (name != "app") {
        extensions.configure<org.gradle.api.plugins.JavaPluginExtension> {
            sourceCompatibility = JavaVersion.VERSION_17
            targetCompatibility = JavaVersion.VERSION_17
        }
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        compilerOptions.freeCompilerArgs.addAll("-Xjsr305=strict")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
