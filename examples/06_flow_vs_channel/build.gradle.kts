plugins {
    application
}

application {
    mainClass.set("com.way.coroutines.examples.flowchannel.MainKt")
}

dependencies {
    implementation(project(":core"))
    implementation(libs.kotlinx.coroutines.core)
}
