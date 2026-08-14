package com.reciply.telegram.processor

import com.pengrad.telegrambot.model.Update

class NoopProcessor : TelegramUpdateProcessor {
    override fun canProcess(update: Update): Boolean {
        return true
    }

    override fun process(update: Update) {
        TODO("Not yet implemented")
    }

}