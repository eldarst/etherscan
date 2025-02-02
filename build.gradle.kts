plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.2"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.liquibase.gradle") version "2.2.0"
    id("nu.studer.jooq") version "9.0"
    kotlin("plugin.serialization") version "1.8.20"
    id("org.jlleitschuh.gradle.ktlint") version "12.1.2"
}

group = "com.etherscan"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

val dbUrl: String = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5432/postgres_db"
val dbUser: String = System.getenv("POSTGRES_USER") ?: "postgres_user"
val dbPassword: String = System.getenv("POSTGRES_PASSWORD") ?: "postgres_password"

dependencies {
    // Spring
    implementation("org.springframework.boot:spring-boot-starter-quartz")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // jOOQ
    implementation("org.springframework.boot:spring-boot-starter-jooq")
    jooqGenerator("org.postgresql:postgresql:42.7.2")

    // PostgreSQL driver
    runtimeOnly("org.postgresql:postgresql:42.7.2")

    // Liquibase
    implementation("org.liquibase:liquibase-core")
    liquibaseRuntime("org.liquibase:liquibase-core")
    liquibaseRuntime("org.postgresql:postgresql:42.7.2")
    liquibaseRuntime("info.picocli:picocli:4.6.3")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // Coroutine
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1")

    // KotlinLogging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")

    // Ktor client
    implementation("io.ktor:ktor-client-core:3.0.3")
    implementation("io.ktor:ktor-client-cio:3.0.3")
    implementation("io.ktor:ktor-client-content-negotiation:3.0.3")
    implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.3")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // Test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

liquibase {
    activities {
        register("main") {
            this.arguments =
                mapOf(
                    "changelogFile" to "src/main/resources/db/changelog/db.changelog-master.yaml",
                    "url" to dbUrl,
                    "username" to dbUser,
                    "password" to dbPassword,
                    "driver" to "org.postgresql.Driver",
                )
        }
    }
    runList = "main"
}

jooq {
    version.set("3.19.18")
    configurations {
        create("main") {
            generateSchemaSourceOnCompilation.set(true)
            jooqConfiguration.apply {
                logging = org.jooq.meta.jaxb.Logging.WARN
                jdbc.apply {
                    driver = "org.postgresql.Driver"
                    url = dbUrl
                    user = dbUser
                    password = dbPassword
                }
                generator.apply {
                    name = "org.jooq.codegen.KotlinGenerator"
                    database.apply {
                        name = "org.jooq.meta.postgres.PostgresDatabase"
                        inputSchema = "public"
                    }
                    generate.apply {
                        isDeprecated = false
                        isRecords = true
                        isImmutablePojos = true
                        isFluentSetters = true
                    }
                    target.apply {
                        packageName = "com.example.generated.jooq"
                    }
                }
            }
        }
    }
}

ktlint {
    filter {
        exclude("**/build/generated-src/jooq/**")
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
tasks.named("generateJooq") {
    dependsOn("update")
}