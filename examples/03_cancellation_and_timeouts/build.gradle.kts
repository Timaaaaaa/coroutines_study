plugins {
    application
}

application {
    mainClass.set("com.way.coroutines.examples.cancellation.MainKt")
}

dependencies {
    implementation(project(":core"))
    implementation(libs.kotlinx.coroutines.core)
}
