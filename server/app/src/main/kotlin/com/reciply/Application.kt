package com.reciply

import com.reciply.di.appModule
import com.reciply.di.databaseModule
import com.reciply.telegram.route.telegramWebhook
import com.typesafe.config.ConfigFactory
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.path
import io.ktor.server.response.*
import io.ktor.server.routing.*
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
