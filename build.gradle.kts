import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    id("jacoco")
    kotlin("jvm")
    id("com.diffplug.spotless") version "6.11.0"
    `maven-publish`
}

val projectTitle: String by project



configure(subprojects) {
    apply(plugin = "com.diffplug.spotless")

    configure<com.diffplug.gradle.spotless.SpotlessExtension> {

        lineEndings = com.diffplug.spotless.LineEnding.UNIX

        fun SourceSet.findSourceFilesToTarget() = allJava.srcDirs.flatMap { srcDir ->
            project.fileTree(srcDir).filter { file ->
                file.name.endsWith(".kt") || (file.name.endsWith(".java") && file.name != "package-info.java")
            }
        }

        kotlin {
            licenseHeaderFile(rootProject.file("LICENSE_HEADER")).updateYearWithLatest(true)

            target(
                project.sourceSets.main.get().findSourceFilesToTarget(),
                project.sourceSets.test.get().findSourceFilesToTarget()
            )
        }
        kotlinGradle {
            target("*.gradle.kts")
            ktlint()
        }

        format("misc") {
            // define the files to apply `misc` to
            target("*.gradle", "*.md", ".gitignore")

            // define the steps to apply to those files
            trimTrailingWhitespace()
            indentWithSpaces()
            endWithNewline()
        }
    }
}

allprojects {
    apply<JavaPlugin>()
    apply(plugin = "jacoco")
    apply(plugin = "maven-publish")

    gradle.projectsEvaluated {
        tasks.withType<JavaCompile> {
            if (project.hasProperty("warn")) {
                options.compilerArgs = options.compilerArgs + "-Xlint:unchecked" + "-Xlint:deprecation"
            } else {
                options.compilerArgs = options.compilerArgs + "-Xlint:-unchecked" + "-Xlint:-deprecation"
            }
        }
    }

    repositories {
        mavenCentral()
        maven(url = "https://maven.pkg.jetbrains.space/kotlin/p/kotlin/kotlin-ide-plugin-dependencies") {
            content {
                includeGroup("org.jetbrains.kotlin")
            }
        }
    }
}

subprojects {
    java.sourceCompatibility = JavaVersion.VERSION_11
    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(java.sourceCompatibility.majorVersion.toInt())
    }

    tasks.withType<KotlinCompile>().all {
        compilerOptions.jvmTarget = JvmTarget.fromTarget(java.sourceCompatibility.toString())
    }

    jacoco {
        toolVersion = "0.8.12"
    }

    tasks.jacocoTestReport {
        reports {
            xml.required.set(true)
            csv.required.set(false)
            html.required.set(false)
        }
    }

    // when subproject has Jacoco pLugin applied we want to generate XML report for coverage
    plugins.withType<JacocoPlugin> {
        tasks["test"].finalizedBy("jacocoTestReport")
    }

    configurations {
        // include compileOnly dependencies during test
        testImplementation {
            extendsFrom(configurations.compileOnly.get())
        }
    }

    tasks.test {
        useJUnitPlatform()

        testLogging {
            exceptionFormat =
                org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL // log the full stack trace (default is the 1st line of the stack trace)
            events("skipped", "failed") // verbose log for failed and skipped tests (by default the name of the tests are not logged)
        }
    }

    java {
        withJavadocJar()
        withSourcesJar()
    }

    publishing {
        publications {
            create<MavenPublication>("mavenJava") {
                from(components["java"])

                pom {
                    name.set(projectTitle)
                    description.set(project.description)
                    url.set("http://www.sonarqube.org/")
                    organization {
                        name.set("SonarSource")
                        url.set("http://www.sonarqube.org/")
                    }
                    licenses {
                        license {
                            name.set("GNU LPGL 3")
                            url.set("http://www.gnu.org/licenses/lgpl.txt")
                            distribution.set("repo")
                        }
                    }
                    scm {
                        url.set("https://github.com/SonarSource/analysis-ast-query")
                    }
                    developers {
                        developer {
                            id.set("sonarsource-team")
                            name.set("SonarSource Team")
                        }
                    }
                }
            }
        }
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk8"))
}

repositories {
    mavenCentral()
}

kotlin {
    jvmToolchain(8)
}