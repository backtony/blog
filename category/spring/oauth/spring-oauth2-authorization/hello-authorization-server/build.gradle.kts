plugins {
    kotlin("plugin.jpa") version "1.9.23"
}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

dependencies {
    // authorization server
    implementation("org.springframework.boot:spring-boot-starter-oauth2-authorization-server")

    // for register user
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.session:spring-session-data-redis")
    implementation("org.apache.commons:commons-pool2:2.12.0")

    // db
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.mysql:mysql-connector-j")
//    runtimeOnly("com.h2database:h2")

    // actuator
    implementation("org.springframework.boot:spring-boot-starter-actuator")
}
