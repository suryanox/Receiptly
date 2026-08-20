package com.reciply

import com.reciply.di.appModule
import com.reciply.di.databaseModule
import com.reciply.telegram.route.telegramWebhook
import com.typesafe.config.ConfigFactory
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import org.koin.ktor.plugin.Koin
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("TelegramWebhook")

fun main() {
    val config = ConfigFactory.load()

    val port = config.getInt("app.port")
    val host = config.getString("app.host")

    embeddedServer(
        Netty,
        port = port,
        host = host,
    ) {
        install(Koin) {
            modules(
                appModule,
                databaseModule,
            )
        }

        install(StatusPages) {
            exception<Throwable> { call, cause ->

                if (call.request.path() == "/telegram/webhook") {
                    logger.error(
                        "Telegram webhook failed",
                        cause,
                    )

                    call.respond(
                        HttpStatusCode.OK,
                    )
                } else {
                    throw cause
                }
            }
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
