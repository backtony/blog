val jar: Jar by tasks
jar.enabled = false

plugins {
    id("sample-springboot")
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))
    implementation(project(":adapter:inbound:listener"))
    implementation(project(":adapter:outbound:repository"))
    implementation(project(":adapter:outbound:producer"))
    implementation(project(":infrastructure:h2"))
//    implementation(project(":infrastructure:mongo"))

    // for actuator
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.sample.hexagonal.common:actuator")
}
