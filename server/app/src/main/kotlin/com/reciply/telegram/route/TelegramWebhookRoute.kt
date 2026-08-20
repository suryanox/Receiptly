package com.reciply.telegram.route

import com.google.gson.Gson
import com.pengrad.telegrambot.model.Update
import com.reciply.telegram.service.TelegramWebhookService
import io.ktor.server.request.receiveText
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.koin.ktor.ext.inject

fun Route.telegramWebhook() {
    val service: TelegramWebhookService by inject()
    val gson = Gson()

    post("/telegram/webhook") {
        val raw = call.receiveText()
        val update = gson.fromJson(raw, Update::class.java)
        service.handleUpdate(update)
        call.respondText("OK")
    }
}
