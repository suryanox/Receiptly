package com.reciply.telegram.processor

import com.pengrad.telegrambot.model.Update

class NoopProcessor : TelegramUpdateProcessor {
    override val order: Int = Int.MAX_VALUE

    override fun canProcess(update: Update): Boolean = true

    override fun process(update: Update) {
        // no-op
    }
}
