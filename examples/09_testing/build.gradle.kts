plugins {
    application
}

application {
    mainClass.set("com.way.coroutines.examples.testing.MainKt")
}

dependencies {
    implementation(project(":core"))
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.junit.jupiter)
}
