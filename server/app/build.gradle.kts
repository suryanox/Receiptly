plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.ktlint)
    application
}

ktlint {
    version = "1.5.0"
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)

    // Telegram
    implementation("com.github.pengrad:java-telegram-bot-api:10.1.0")

    // Configuration
    implementation("com.typesafe:config:1.4.3")

    // Koin
    implementation("io.insert-koin:koin-ktor:4.1.1")
    implementation("io.insert-koin:koin-logger-slf4j:4.1.1")
    implementation("io.ktor:ktor-server-status-pages:3.3.1")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.18")

    // Exposed
    implementation("org.jetbrains.exposed:exposed-core:0.61.0")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.61.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.61.0")

    // PDF generation
    implementation("com.itextpdf:itext7-core:7.2.3")

    // Database
    implementation("com.zaxxer:HikariCP:6.2.1")
    implementation("org.postgresql:postgresql:42.7.4")
}

testing {
    suites {
        val test =
            named<JvmTestSuite>("test") {
                useKotlinTest("2.4.0")
            }
    }
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

application {
    mainClass = "com.reciply.ApplicationKt"
}
