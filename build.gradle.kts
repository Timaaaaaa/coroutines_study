// Root build configuration for the coroutine study project.
buildscript {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        google()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:2.0.21")
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    group = "com.way.coroutines"
    version = "1.0.0"

    repositories {
        mavenCentral()
        google()
    }

    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions.jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        compilerOptions.freeCompilerArgs.addAll("-Xjsr305=strict")
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
    }
}
