package com.reciply.telegram.service

import com.reciply.telegram.model.TelegramRequestContext
import com.reciply.telegram.model.toRequestContext
import com.pengrad.telegrambot.model.Update
import com.reciply.telegram.processor.TelegramUpdateProcessor
import org.slf4j.LoggerFactory

class TelegramWebhookService(
    private val processors: List<TelegramUpdateProcessor>
) {
    private val log = LoggerFactory.getLogger(TelegramWebhookService::class.java)

    suspend fun handleUpdate(update: Update) {
        val context = update.toRequestContext()
        log.info("Processing: type=${context.messageType}, text=${context.text}")
        val processor = processors.firstOrNull { it.canProcess(context) }
        log.info("Processor found: ${processor?.javaClass?.simpleName}")
        processor?.process(context)
    }
}
