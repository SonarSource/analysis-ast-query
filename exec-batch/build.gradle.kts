plugins {
    kotlin("jvm")
}

dependencies {
    compileOnly(libs.slf4j.api)
    implementation(libs.kotlin.compiler)
    implementation(project(":core"))

    testImplementation(testLibs.junit.jupiter)
    testImplementation(libs.slf4j.api)
    testImplementation(testLibs.assertj.core)
    testImplementation(testLibs.mockito.core)
    testImplementation(testLibs.mockk)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}