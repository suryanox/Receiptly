package com.reciply.telegram.route

import com.pengrad.telegrambot.model.Update
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.telegramWebhook() {
    val service: TelegramWebhookService by inject()

    post("/telegram/webhook") {
        val update = call.receive<Update>()
        service.handleUpdate(update)
        call.respondText("OK")
    }
}
