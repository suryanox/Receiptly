package com.reciply.telegram.service

import com.reciply.telegram.model.toRequestContext
import com.pengrad.telegrambot.model.Update
import com.reciply.telegram.processor.TelegramUpdateProcessor

class TelegramWebhookService(
    private val processors: List<TelegramUpdateProcessor>
) {
    suspend fun handleUpdate(update: Update) {
        val context = update.toRequestContext() ?: return
        processors.firstOrNull { it.canProcess(context) }?.process(context)
    }
}
