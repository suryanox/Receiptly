package com.reciply.telegram

import com.pengrad.telegrambot.model.Update
import com.reciply.telegram.processor.TelegramUpdateProcessor

class TelegramWebhookService(
    private val processors: List<TelegramUpdateProcessor>
) {
    suspend fun handleUpdate(update: Update) {
        processors.sortedBy { it.order }
            .firstOrNull { it.canProcess(update) }
            ?.process(update)
    }
}
