package com.reciply.telegram.processor

import com.pengrad.telegrambot.model.Update

class MessageProcessor : TelegramUpdateProcessor {
    override val order: Int = 3
    override fun canProcess(update: Update): Boolean {
        val message = update.message() ?: return false
        return message.photo() == null && message.text()?.startsWith("/") != true
    }

    override fun process(update: Update) {
        // TODO: implement
    }
}
