import com.google.protobuf.gradle.id
import com.google.protobuf.gradle.protobuf
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    id("com.google.protobuf") version "0.9.4"
}

val grpcKotlinVersion = "1.4.1"
val grpcProtoVersion = "1.63.0"
val grpcVersion = "3.25.3"

tasks.getByName<BootJar>("bootJar") {
    enabled = false
}

tasks.getByName<Jar>("jar") {
    enabled = true
}

// https://github.com/grpc/grpc-kotlin/blob/master/examples/stub/build.gradle.kts
dependencies {
    protobuf(project(":protos"))

    api("io.grpc:grpc-stub:$grpcProtoVersion")
    api("io.grpc:grpc-protobuf:$grpcProtoVersion")
    api("io.grpc:grpc-kotlin-stub:$grpcKotlinVersion") // kotlin stub 제공
    api("com.google.protobuf:protobuf-kotlin:$grpcVersion") // kotlin 코드 생성 도구

    api("io.grpc:grpc-netty:$grpcProtoVersion") // stub NettyChannel에 사용
}

protobuf {
    // Configure the protoc executable.
    protoc {
        // Download from the repository.
        artifact = "com.google.protobuf:protoc:$grpcVersion"
    }

    // Locate the codegen plugins.
    plugins {
        // Locate a plugin with name 'grpc'.
        id("grpc") {
            // Download from the repository.
            artifact = "io.grpc:protoc-gen-grpc-java:$grpcProtoVersion"
        }
        // Locate a plugin with name 'grpcKt'.
        id("grpckt") {
            // Download from the repository.
            artifact = "io.grpc:protoc-gen-grpc-kotlin:$grpcKotlinVersion:jdk8@jar"
        }
    }

    // generate code
    generateProtoTasks {
        all().forEach {
            it.plugins {
                id("grpc")
                id("grpckt")
            }
            it.builtins {
                id("kotlin")
            }
        }
    }
}
