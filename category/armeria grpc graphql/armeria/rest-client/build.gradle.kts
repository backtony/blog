dependencies {
    implementation(project(":stub"))

    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // for grpc client
    implementation("com.linecorp.armeria:armeria-grpc:1.27.0")
}
