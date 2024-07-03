val jar: Jar by tasks
jar.enabled = false

plugins {
    id("sample-springboot")
}

dependencies {
    implementation(project(":domain"))
    implementation(project(":application"))
    implementation(project(":adapter:inbound:controller"))
    implementation(project(":adapter:outbound:repository"))
    implementation(project(":adapter:outbound:producer"))
    implementation(project(":infrastructure:h2"))

//    implementation(project(":infrastructure:mongo"))

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.sample.hexagonal.common:actuator")

    // 명시적으로 확인하기 위해서 추가
    implementation("com.sample.hexagonal.common:utils")
    implementation("com.sample.hexagonal.common:json")
    implementation("com.sample.hexagonal.common:kafka-producer")
    implementation("com.sample.hexagonal.common:exception")
}
