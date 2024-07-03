import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("sample-springboot")
}

dependencies {
    implementation("com.sample.hexagonal.common:utils")

    api("org.springframework.boot:spring-boot-starter-actuator")
    api("io.micrometer:micrometer-core")
    api("io.micrometer:micrometer-registry-prometheus")
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

tasks.jar {
    enabled = true
}
