// Root build configuration for the coroutine study project.
plugins {
    alias(libs.plugins.kotlin.jvm) apply false
}

subprojects {
    if (name == "app") return@subprojects

    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "com.way.coroutines"
    version = "1.0.0"

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        compilerOptions.freeCompilerArgs.addAll("-Xjsr305=strict")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
