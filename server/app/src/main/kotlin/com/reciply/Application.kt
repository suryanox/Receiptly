package com.reciply

import com.reciply.di.appModule
import com.reciply.telegram.route.telegramWebhook
import com.typesafe.config.ConfigFactory
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.plugin.Koin

fun main() {
    val config = ConfigFactory.load()
    val port = config.getInt("app.port")
    val host = config.getString("app.host")

    embeddedServer(Netty, port = port, host = host) {
        install(Koin) {
            modules(appModule)
        }
        configureRouting()
    }.start(wait = true)
}

fun Application.configureRouting() {
    routing {
        get("/health") {
            call.respondText("OK")
        }
        telegramWebhook()
    }
}
