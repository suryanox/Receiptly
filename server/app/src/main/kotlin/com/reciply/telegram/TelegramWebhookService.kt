package com.reciply.telegram

import com.pengrad.telegrambot.model.Update
import com.reciply.telegram.processor.TelegramUpdateProcessor

class TelegramWebhookService(
    private val processors: List<TelegramUpdateProcessor>
) {
    fun handleUpdate(update: Update) {
        processors.firstOrNull { it.canProcess(update) }?.process(update)
    }
}
