package com.reciply.telegram.processor

import com.pengrad.telegrambot.model.Update
import com.reciply.telegram.TelegramReplyService

class ImageProcessor(private val replyService: TelegramReplyService) : TelegramUpdateProcessor {
    override val order: Int = 2

    override fun canProcess(update: Update): Boolean {
        return update.message()?.photo()?.isNotEmpty() == true
    }

    override suspend fun process(update: Update) {
        // TODO: implement
    }
}
