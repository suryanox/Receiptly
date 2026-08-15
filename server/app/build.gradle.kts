plugins {
    alias(libs.plugins.kotlin.jvm)
    application
}

repositories {
    mavenCentral()
}

dependencies {
    // Ktor
    implementation("io.ktor:ktor-server-core-jvm:3.3.1")
    implementation("io.ktor:ktor-server-netty-jvm:3.3.1")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:3.3.1")
    implementation("io.ktor:ktor-serialization-jackson-jvm:3.3.1")

    // Telegram
    implementation("com.github.pengrad:java-telegram-bot-api:10.1.0")

    // Config
    implementation("com.typesafe:config:1.4.3")

    // Koin
    implementation("io.insert-koin:koin-ktor:4.1.0")
    implementation("io.insert-koin:koin-logger-slf4j:4.1.0")

    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.18")
}

testing {
    suites {
        val test = named<JvmTestSuite>("test") {
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