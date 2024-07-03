val jar: Jar by tasks
jar.enabled = false

plugins {
    id("sample-springboot")
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))
    implementation(project(":adapter:inbound:job"))
    implementation(project(":adapter:outbound:producer"))
    implementation(project(":adapter:outbound:repository"))

//    implementation("com.sample.hexagonal.common:kafka-producer")
    implementation("com.sample.hexagonal.common:actuator")

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("com.google.guava:guava:32.0.1-jre")

    runtimeOnly("com.h2database:h2")
}
