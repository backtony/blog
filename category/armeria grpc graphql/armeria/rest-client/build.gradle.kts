dependencies {
    implementation(project(":stub"))

    implementation("org.springframework.boot:spring-boot-starter-webflux")

    // for zipkin
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.reporter2:zipkin-reporter-brave")
    implementation("io.zipkin.reporter2:zipkin-sender-kafka")
    implementation("io.zipkin.brave:brave-instrumentation-grpc")
}
