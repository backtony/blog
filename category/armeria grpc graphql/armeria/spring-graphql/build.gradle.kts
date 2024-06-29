import com.kobylynskyi.graphql.codegen.model.ApiInterfaceStrategy
import com.kobylynskyi.graphql.codegen.model.ApiNamePrefixStrategy
import com.kobylynskyi.graphql.codegen.model.ApiRootInterfaceStrategy
import com.kobylynskyi.graphql.codegen.model.GeneratedLanguage
import graphql.parser.ParserOptions
import io.github.kobylynskyi.graphql.codegen.gradle.GraphQLCodegenGradleTask
import org.jetbrains.kotlin.gradle.internal.KaptGenerateStubsTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("io.github.kobylynskyi.graphql.codegen") version "5.10.0"
}

dependencies {
    implementation(project(":stub"))

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // graphql
    implementation("org.springframework.boot:spring-boot-starter-graphql")
    implementation("org.springframework.data:spring-data-commons")
    // graphql에서 default로 지원하지 않는 타입 추가를 위한 의존성
    implementation("com.graphql-java:graphql-java-extended-scalars:22.0")
    // dateTime scalar를 위한 의존성
    implementation("com.tailrocks.graphql:graphql-datetime-spring-boot-starter:6.0.0")
    // for gql schema cache
    implementation("com.github.ben-manes.caffeine:caffeine")

    // directive validation
    implementation("com.graphql-java:graphql-java-extended-validation:22.0")

    // for zipkin
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
    implementation("io.zipkin.reporter2:zipkin-reporter-brave")
    implementation("io.zipkin.reporter2:zipkin-sender-kafka")
    implementation("io.zipkin.brave:brave-instrumentation-grpc")
}

// graphql
// https://kobylynskyi.github.io/graphql-java-codegen/plugins/gradle/
tasks.named<GraphQLCodegenGradleTask>("graphqlCodegen") {
    // all config options:
    // https://github.com/kobylynskyi/graphql-java-codegen/blob/main/docs/codegen-options.md
    outputDir = layout.buildDirectory.dir("generated").get().asFile
    generatedLanguage = GeneratedLanguage.KOTLIN
    modelPackageName = "com.example.springgraphql.model"
    apiPackageName = "com.example.springgraphql.api"
    generateApisWithSuspendFunctions = true // suspend
    generateDataFetchingEnvironmentArgumentInApis = true // env

    apiInterfaceStrategy = ApiInterfaceStrategy.DO_NOT_GENERATE
    apiRootInterfaceStrategy = ApiRootInterfaceStrategy.INTERFACE_PER_SCHEMA
    apiNamePrefixStrategy = ApiNamePrefixStrategy.FOLDER_NAME_AS_PREFIX

    // validation
    directiveAnnotationsMapping = mapOf(
        "min" to listOf("@jakarta.validation.constraints.Min(value={{value}})"),
        "max" to listOf("@jakarta.validation.constraints.Max(value={{value}})"),
        "notBlank" to listOf("@jakarta.validation.constraints.NotBlank"),
        "notEmpty" to listOf("@jakarta.validation.constraints.NotEmpty"),
        "size" to listOf("@jakarta.validation.constraints.Size(min={{min}}, max={{max}})")
    )

    resolverArgumentAnnotations = setOf("org.springframework.graphql.data.method.annotation.Argument", "jakarta.validation.Valid")

    // https://github.com/kobylynskyi/graphql-java-codegen/issues/983#issue-1280078675
    customAnnotationsMapping = mapOf(
        "^Query\\.\\w+\$" to listOf("org.springframework.graphql.data.method.annotation.QueryMapping"),
        "^Mutation\\.\\w+$" to listOf("org.springframework.graphql.data.method.annotation.MutationMapping"),
        "^Subscription\\.\\w+$" to listOf("org.springframework.graphql.data.method.annotation.SubscriptionMapping"),
    )

    // https://github.com/kobylynskyi/graphql-java-codegen/issues/644#issuecomment-932054916
    // https://github.com/kobylynskyi/graphql-java-codegen/issues/1019
    customTypesMapping = mutableMapOf<String, String>(
        "Long" to "Long",
        "LocalDateTime" to "java.time.LocalDateTime",
        "LocalDate" to "java.time.LocalDate",
        "JSON" to "Any",
    )

    // https://github.com/kobylynskyi/graphql-java-codegen/issues/1216
    ParserOptions.setDefaultParserOptions(
        ParserOptions.getDefaultParserOptions().transform { o -> o.maxTokens(Int.MAX_VALUE) },
    )
}

// Automatically generate GraphQL code on project build:
sourceSets {
    getByName("main").java.srcDirs(layout.buildDirectory.dir("generated").get().asFile)
//    getByName("main").kotlin.srcDirs(layout.buildDirectory.dir("generated").get().asFile)
}

// Add generated sources to your project source sets:
tasks.named<KotlinCompile>("compileKotlin") {
    dependsOn("graphqlCodegen")
}

// https://stackoverflow.com/questions/78369549/how-to-set-dependency-for-gradle-task-kaptgeneratestubskotlin-to-openapigener
tasks.withType<KaptGenerateStubsTask> {
    dependsOn(tasks.named("graphqlCodegen"))
}
