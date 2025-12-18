plugins {
    application
}

application {
    mainClass.set("com.way.coroutines.examples.basics.MainKt")
}

dependencies {
    implementation(project(":core"))
    implementation(libs.kotlinx.coroutines.core)
}
