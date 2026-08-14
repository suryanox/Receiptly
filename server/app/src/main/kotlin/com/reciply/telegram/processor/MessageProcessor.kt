package com.reciply.telegram.processor

import com.pengrad.telegrambot.model.Update
import com.reciply.telegram.TelegramReplyService

class MessageProcessor(private val replyService: TelegramReplyService) : TelegramUpdateProcessor {
    override val order: Int = 3

    override fun canProcess(update: Update): Boolean {
        val message = update.message() ?: return false
        return message.photo() == null && message.text()?.startsWith("/") != true
    }

    override suspend fun process(update: Update) {
        // TODO: implement
    }
}
