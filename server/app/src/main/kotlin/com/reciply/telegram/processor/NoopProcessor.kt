package com.reciply.telegram.processor

import com.pengrad.telegrambot.model.Update
import com.reciply.telegram.TelegramReplyService

class NoopProcessor(private val replyService: TelegramReplyService) : TelegramUpdateProcessor {
    override val order: Int = Int.MAX_VALUE

    override fun canProcess(update: Update): Boolean = true

    override suspend fun process(update: Update) {

    }
}
