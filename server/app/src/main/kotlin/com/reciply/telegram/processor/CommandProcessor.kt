package com.reciply.telegram.processor

import com.pengrad.telegrambot.model.Update
import com.reciply.telegram.TelegramReplyService

class CommandProcessor(private val replyService: TelegramReplyService) : TelegramUpdateProcessor {
    override val order: Int = 1

    override fun canProcess(update: Update): Boolean {
        return update.message()?.text()?.startsWith("/") == true
    }

    override suspend fun process(update: Update) {
        // TODO: implement
    }
}
