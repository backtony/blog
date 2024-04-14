val armeriaVersion = "1.27.0"

dependencies {
    implementation(project(":stub"))

    // armeria
    // https://github.com/line/armeria-examples/blob/main/grpc/build.gradle
    implementation(platform("io.netty:netty-bom:4.1.106.Final"))
    implementation(platform("com.linecorp.armeria:armeria-bom:$armeriaVersion"))
    implementation("com.linecorp.armeria:armeria-kotlin:$armeriaVersion")
    implementation("com.linecorp.armeria:armeria-spring-boot3-starter:$armeriaVersion")
    implementation("com.linecorp.armeria:armeria-spring-boot3-actuator-starter:$armeriaVersion")

    // grpc
    implementation("com.linecorp.armeria:armeria-grpc:$armeriaVersion")

    // r2dbc
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("io.asyncer:r2dbc-mysql:1.1.0")
}
