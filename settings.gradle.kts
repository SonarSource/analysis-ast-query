pluginManagement {
    repositories {
        gradlePluginPortal()
    }

    val kotlinVersion: String by settings
    plugins {
        id("org.jetbrains.kotlin.jvm") version kotlinVersion
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "ast-query"

include(":core")
include(":exec-batch")
include(":exec-greedy")

dependencyResolutionManagement {
    versionCatalogs {
        val kotlinVersion: String by extra
        val slf4jApi = "1.7.30"

        create("libs") {
            library("kotlin-compiler", "org.jetbrains.kotlin", "kotlin-compiler").version(kotlinVersion)
            library("slf4j-api", "org.slf4j", "slf4j-api").version(slf4jApi)
        }

        create("utilLibs") {
            val detekt = version("detekt", "1.23.3")
            val ktlint = version("ktlint", "1.0.1")

            library("detekt-api", "io.gitlab.arturbosch.detekt", "detekt-api").versionRef(detekt)
            library("detekt-cli", "io.gitlab.arturbosch.detekt", "detekt-cli").versionRef(detekt)
            library("detekt-core", "io.gitlab.arturbosch.detekt", "detekt-core").versionRef(detekt)
            library("ktlint-core", "com.pinterest.ktlint", "ktlint-rule-engine-core").versionRef(ktlint)
            library("ktlint-ruleset-standard", "com.pinterest.ktlint", "ktlint-ruleset-standard").versionRef(ktlint)

            bundle("detekt", listOf("detekt-cli", "detekt-core", "detekt-api"))
            bundle("ktlint", listOf("ktlint-core", "ktlint-ruleset-standard"))
        }

        create("testLibs") {
            val assertj = version("assertj", "3.24.2")
            val junit = version("junit", "5.10.1")
            val mockito = version("mockito", "5.7.0")
            val mockk = version("mockk", "1.13.3")

            library("assertj-core", "org.assertj", "assertj-core").versionRef(assertj)
            library("junit-jupiter", "org.junit.jupiter", "junit-jupiter").versionRef(junit)
            library("mockito-core", "org.mockito", "mockito-core").versionRef(mockito)
            library("mockk", "io.mockk", "mockk").versionRef(mockk)
        }
    }
}
