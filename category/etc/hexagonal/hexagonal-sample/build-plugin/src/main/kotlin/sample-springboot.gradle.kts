plugins {
    // https://discuss.gradle.org/t/applying-a-plugin-version-inside-a-convention-plugin/42160
    // https://blog.sapzil.org/2022/03/04/gradle-convention-plugins/
    // 플러그인의 버전은 현재 프로젝트의 build.gradle.kts에 명시해야만 합니다.
    id("sample-kotlin-jvm")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
}

extra["springCloudVersion"] = "2023.0.0"

dependencies {
    kapt("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.3")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
}
