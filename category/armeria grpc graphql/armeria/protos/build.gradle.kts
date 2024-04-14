import org.springframework.boot.gradle.tasks.bundling.BootJar

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

java {
    sourceSets.getByName("main").resources.srcDir("src/main/proto")
}
