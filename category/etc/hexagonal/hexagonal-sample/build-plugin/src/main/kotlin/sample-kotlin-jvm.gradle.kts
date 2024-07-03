import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    // https://discuss.gradle.org/t/applying-a-plugin-version-inside-a-convention-plugin/42160
    // https://blog.sapzil.org/2022/03/04/gradle-convention-plugins/
    // 플러그인의 버전은 현재 프로젝트의 build.gradle.kts에 명시해야만 합니다.
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlinx.kover")
    // for implementation, java, etc..
    id("java-library")

    kotlin("jvm")
    kotlin("kapt")
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    testImplementation("io.kotest:kotest-runner-junit5-jvm:5.8.0")
    testImplementation("io.kotest:kotest-assertions-core-jvm:5.8.0")
    testImplementation("io.kotest:kotest-framework-datatest:5.8.0")
    testImplementation("io.mockk:mockk:1.13.8")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

// https://github.com/JLLeitschuh/ktlint-gradle?tab=readme-ov-file#configuration
ktlint {
    reporters {
        reporter(ReporterType.CHECKSTYLE)
        reporter(ReporterType.HTML)
    }
}

// https://github.com/Kotlin/kotlinx-kover/blob/main/kover-gradle-plugin/examples/jvm/defaults/build.gradle.kts
koverReport {
    filters {
        includes {
            classes("com.sample.hexagonal.*")
        }
    }
}
