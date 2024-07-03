import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("sample-springboot")
}

dependencies {
    implementation(project(":application"))

    implementation("com.sample.hexagonal.common:kafka-producer")
    implementation("com.sample.hexagonal.common:utils")
    implementation("com.sample.hexagonal.common:json")
}

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

tasks.jar {
    enabled = true
}
